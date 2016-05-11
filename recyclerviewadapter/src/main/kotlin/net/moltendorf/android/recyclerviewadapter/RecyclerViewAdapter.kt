package net.moltendorf.android.recyclerviewadapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import java.lang.reflect.ParameterizedType
import java.util.*

/**
 * More generic implementation of RecyclerView.
 * Can be used in various ways with minimal, if any, extension required.
 */
class RecyclerViewAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder<Any>>() {
  private var dataSet: List<Any> = Collections.emptyList();
  private var typeLookup: Map<Any, Int> = HashMap()
  private var factoryLookup = ArrayList<Factory<ViewHolder<Any>>>(0)

  override fun getItemViewType(position: Int): Int {
    // @todo Add more helpful exception when no class to resource relationship exists.
    val data = dataSet[position];

    val type = if (data is ViewType) {
      typeLookup[data.type] ?: typeLookup[data.javaClass]
    } else {
      typeLookup[data.javaClass]
    }

    return type ?: -1;
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Any> {
    return factoryLookup[viewType].createViewHolder(context, parent) // @todo Note above @todo: This may throw due to that.
  }

  override fun onBindViewHolder(holder: ViewHolder<Any>, position: Int) {
    holder.bindTo(dataSet[position], position)
  }

  override fun getItemCount(): Int {
    return dataSet.size
  }

  fun setViewHolders(vararg holders: Class<*>) {
    val factories = LinkedHashSet<Factory<ViewHolder<Any>>>()

    outer@for (holder in holders) {
      for (possible in holder.declaredClasses) {
        if (Factory::class.java.isAssignableFrom(possible)) {
          try {
            factories.add(possible.newInstance() as Factory<ViewHolder<Any>>)
          } catch (exception: Exception) {
            throw RuntimeException("Could not instantiate Factory.", exception)
          }

          continue@outer
        }
      }

      throw RuntimeException("No ViewHolder Factory inside of " + holder.simpleName)
    }

    setViewHolders(factories)
  }

  fun setViewHolders(factories: Set<Factory<ViewHolder<Any>>>) {
    // Create lookups.
    val size = factories.size
    val typeLookup = HashMap<Any, Int>(size)
    val factoryLookup = ArrayList<Factory<ViewHolder<Any>>>(size)

    var i = 0
    for (factory in factories) {
      typeLookup[factory.type] = i
      factoryLookup[i] = factory

      ++i
    }

    this.typeLookup = typeLookup;
    this.factoryLookup = factoryLookup;
  }

  fun changeDataSet(dataSet: List<Any>) {
    this.dataSet = dataSet

    notifyDataSetChanged()
  }

  abstract class Factory<T : ViewHolder<Any>> {
    open val type: Any = (((javaClass.getGenericSuperclass() as ParameterizedType).actualTypeArguments[0] as Class<*>).genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>

    override fun equals(other: Any?): Boolean {
      if (this === other) {
        return true
      }

      if (other is Factory<*>) {
        return type == other.type
      }

      return false
    }

    override fun hashCode(): Int {
      return type.hashCode()
    }

    abstract fun createViewHolder(context: Context, parent: ViewGroup): T
  }

  abstract class ViewHolder<T>(protected var context: Context, viewGroup: ViewGroup, resource: Int) : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(resource, viewGroup, false)) {
    protected var data: T? = null
    protected var index: Int = 0

    internal fun bindTo(data: Any, index: Int) {
      this.data = data as? T
      this.index = index

      bindTo()
    }

    abstract fun bindTo()
  }

  companion object {
    private val TAG = "PlacesListAdapter"
  }
}
