RecyclerViewAdapter
===================

> Base implementation of RecyclerViewAdapter that avoids the need to directly build your own adapter. 

## Overview

This project is mostly proof of concept and updates may include breaking changes.

## Usage

This project currently uses Git for distribution, so you'll need JitPack.
 
```gradle
repositories {
  maven { url 'https://jitpack.io' }
}
```

Then just add this dependency to your project.

```gradle
dependencies {
  compile 'com.github.moltendorf:RecyclerViewAdapter:0.4'
}
```

Now you can import the class anywhere in your project.
 
```java 
import net.moltendorf.android.recyclerviewadapter.RecyclerViewAdapter;
```

Create your view holders and data models.

```java
package com.example;

import android.content.Context;
import android.view.ViewGroup;

import net.moltendorf.android.recyclerviewadapter.RecyclerViewAdapter;

public class ExampleViewHolder extends RecyclerViewAdapter.ViewHolder<ExampleData> {
  public ExampleViewHolder(Context context, ViewGroup viewGroup) {
    super(context, viewGroup, android.R.layout.simple_list_item_1);
  }

  @Override
  public void bindTo() {
    ((TextView) itemView).setText(object.getText());
  }

  public static class Factory extends RecyclerViewAdapter.Factory<ExampleViewHolder> {
    @Override
    public ExampleViewHolder createViewHolder(Context context, ViewGroup parent) {
      return new ExampleViewHolder(context, parent);
    }
  }
}

public class ExampleData {
  private String text = "Example Text";
  
  public String getText() {
    return text;
  }
}

```

Then just create a new RecyclerViewAdapter and bind your view holders to it.

```java
// Create some example data.
List exampleList = new ArrayList(1);
exampleList.add(new ExampleData());

RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext());
adapter.setViewHolders(ExampleViewHolder.class);
adapter.changeDataSet(exampleList);
```

## Code Style

This project is indented with 2 spaces and wraps after the 128th column for all code files. All files must end with a newline.

Why spaces? GitHub uses 8 spaces per tab in code previews which would cause code to show up differently.
  
Why 128 columns? Because at 10pt font, you can fit three files on a 2560x1440 monitor. Also, GitHub code viewer.
