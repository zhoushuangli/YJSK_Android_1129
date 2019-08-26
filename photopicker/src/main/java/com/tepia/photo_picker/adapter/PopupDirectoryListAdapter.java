package com.tepia.photo_picker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.tepia.photo_picker.R;
import com.tepia.photo_picker.entity.PhotoDirectory;

import java.util.ArrayList;
import java.util.List;

/*************************************************************
 * Created by OCN.YAN                                        *
 * 主要功能:TOTO                                             *
 * 项目名:photopicker                                        *
 * 包名:com.tepia.photo_picker adapter                       *
 * 创建时间:2016年06月14日14:40                              *
 * 更新人:yanqiuqiu                                          *
 * 邮箱:yanqqkongmi@gmail.com                                *
 * QQ:2361167552                                             *
 * 更新时间:2017年09月14日14:40                              *
 * 版权:个人版权所有                                         *
 * 版本号:1.1.0                                              *
 *************************************************************/
public class PopupDirectoryListAdapter extends BaseAdapter {


  private List<PhotoDirectory> directories = new ArrayList<>();
  private RequestManager glide;

  public PopupDirectoryListAdapter(RequestManager glide, List<PhotoDirectory> directories) {
    this.directories = directories;
    this.glide = glide;
  }


  @Override public int getCount() {
    return directories.size();
  }


  @Override public PhotoDirectory getItem(int position) {
    return directories.get(position);
  }


  @Override public long getItemId(int position) {
    return directories.get(position).hashCode();
  }


  @Override public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      LayoutInflater mLayoutInflater = LayoutInflater.from(parent.getContext());
      convertView = mLayoutInflater.inflate(R.layout.picker_item_directory, parent, false);
      holder = new ViewHolder(convertView);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    holder.bindData(directories.get(position));

    return convertView;
  }

  private class ViewHolder {

    public ImageView ivCover;
    public TextView tvName;
    public TextView tvCount;

    public ViewHolder(View rootView) {
      ivCover = rootView.findViewById(R.id.iv_dir_cover);
      tvName  = rootView.findViewById(R.id.tv_dir_name);
      tvCount = rootView.findViewById(R.id.tv_dir_count);
    }

    public void bindData(PhotoDirectory directory) {
      final RequestOptions options = new RequestOptions();
      options.dontAnimate()
          .dontTransform()
          .override(800, 800)
          .placeholder(R.drawable.picker_ic_photo_black_48dp)
          .error(R.drawable.picker_ic_broken_image_black_48dp);
      glide.setDefaultRequestOptions(options)
          .load(directory.getCoverPath())
          .thumbnail(0.1f)
          .into(ivCover);
      tvName.setText(directory.getName());
      tvCount.setText(tvCount.getContext().getString(R.string.picker_image_count, directory.getPhotos().size()));
    }
  }

}
