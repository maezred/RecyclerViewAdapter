package net.moltendorf.android.recyclerviewadapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * More generic implementation of RecyclerView.
 * Can be used in various ways with minimal, if any, extension required.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
  private static final String TAG = "PlacesListAdapter";

  private Context context;
  private List dataSet;

  private Map<Class<?>, Integer> viewTypeLookup;
  private Factory[] viewHolderLookup;

  public RecyclerViewAdapter(Context context) {
    this(context, Collections.<Factory>emptySet());
  }

  public RecyclerViewAdapter(Context context, Set<Factory> factories) {
    this(context, factories, Collections.EMPTY_LIST);
  }

  public RecyclerViewAdapter(Context context, Set<Factory> factories, List dataSet) {
    Log.d(TAG, "PlacesListAdapter: Called.");

    this.context = context;
    this.dataSet = dataSet;

    setViewHolders(factories);
  }

  @Override
  public int getItemViewType(int position) {
    // Todo: add more helpful exception when no class to resource relationship exists.
    return viewTypeLookup.get(dataSet.get(position).getClass());
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Factory factory = viewHolderLookup[viewType];
    ViewHolder viewHolder = factory.createViewHolder(context, parent);

    factory.subject.onNext(viewHolder);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    holder.bindTo(dataSet.get(position), position);
  }

  @Override
  public int getItemCount() {
    return dataSet.size();
  }

  public void setViewHolders(Class<?>... holders) {
    Set<Factory> factories = new LinkedHashSet<>();

    outer:
    for (Class<?> holder : holders) {
      for (Class<?> possible : holder.getDeclaredClasses()) {
        if (Factory.class.isAssignableFrom(possible)) {
          try {
            factories.add((Factory) possible.newInstance());
          } catch (Exception exception) {
            throw new RuntimeException("Could not instantiate Factory.", exception);
          }

          continue outer;
        }
      }

      throw new RuntimeException("No ViewHolder Factory inside of " + holder.getSimpleName());
    }

    setViewHolders(factories);
  }

  public void setViewHolders(Set<Factory> factories) {
    // Create lookups.
    int size = factories.size();
    viewTypeLookup = new HashMap<>(size);
    viewHolderLookup = new Factory[size];

    int i = 0;
    for (Factory factory : factories) {
      viewTypeLookup.put(factory.getDataClass(), i);
      viewHolderLookup[i] = factory;

      ++i;
    }
  }

  public void changeDataSet(List dataSet) {
    this.dataSet = dataSet;

    notifyDataSetChanged();
  }

  abstract public static class ViewHolder<T> extends RecyclerView.ViewHolder {

    protected Context context;
    protected T object;
    protected int position;

    public ViewHolder(Context context, ViewGroup viewGroup, int resource) {
      super(LayoutInflater.from(context).inflate(resource, viewGroup, false));

      this.context = context;
    }

    private void bindTo(Object object, int position) {
      this.object = (T) object;
      this.position = position;

      bindTo();
    }

    abstract public void bindTo();
  }

  abstract public static class Factory<T extends ViewHolder> {
    private Class<?> dataClass;
    private PublishSubject<T> subject = PublishSubject.create();

    public Factory() {
      Class<?> viewHolderClass = (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
      dataClass = (Class<?>) ((ParameterizedType) viewHolderClass.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public Class<?> getDataClass() {
      return dataClass;
    }

    public Observable<T> created() {
      return subject;
    }

    @Override
    public final boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o instanceof Factory) {
        Factory factory = (Factory) o;

        return dataClass.equals(factory.dataClass);
      }

      return false;
    }

    @Override
    public final int hashCode() {
      return dataClass.hashCode();
    }

    abstract public T createViewHolder(Context context, ViewGroup parent);
  }
}
