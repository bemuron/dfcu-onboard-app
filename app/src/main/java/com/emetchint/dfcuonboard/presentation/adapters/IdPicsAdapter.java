package com.emetchint.dfcuonboard.presentation.adapters;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.emetchint.dfcuonboard.R;
import com.emetchint.dfcuonboard.models.IdPic;

import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class IdPicsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final String TAG = IdPicsAdapter.class.getSimpleName();
  //private ArrayList<Uri> uriArrayList;
  private List<IdPic> idPics;
  private LayoutInflater inflater;
  private Context context;
  private OnItemClickListener onItemClickListener;
  private final static int IMAGE_LIST = 0;
  private final static int IMAGE_PICKER = 1;
  private IdPicsAdapterListener listener;
  private SparseBooleanArray selectedItems;
  private String activity;

  public class ImageUploadViewHolder extends RecyclerView.ViewHolder {
    public TextView title, content, date, iconText, issueName;
    public ImageView imageView;
    public LinearLayout mustHaveContainer;
    public RelativeLayout iconContainer, iconBack, iconFront, imgContainer;

    public ImageUploadViewHolder(View view) {
      super(view);
      //imageView = view.findViewById(R.id.jobPic);
    }
  }

  public IdPicsAdapter(Context context, List<IdPic> idPicList, String callingActivity,
                       IdPicsAdapterListener listener) {
    inflater = LayoutInflater.from(context);
    this.context = context;
    this.idPics = idPicList;
    this.listener = listener;
    this.activity = callingActivity;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.id_pic_list_item, parent, false);
      return new ImageListViewHolder(view);
  }

 /* @Override
  public int getItemViewType(int position) {
    return position < 2 ? IMAGE_PICKER : IMAGE_LIST;
  }*/

  @Override
  public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
    IdPic idPic = idPics.get(position);

    try {
      if (activity.equals("my_profile")){
        Glide.with(context)
                .load("https://www.emtechint.com/dfcu/public/assets/images/ids/" + idPic.getImage_name())
                //.placeholder(R.color.codeGray)
                //.centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(((ImageListViewHolder) holder).image);
      }else{
        Glide.with(context)
                .load("https://www.emtechint.com/dfcu/public/assets/images/job_pics/" + idPic.getImage_name())
                //.placeholder(R.color.codeGray)
                //.centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(((ImageListViewHolder) holder).image);
      }

    }catch (Exception e){
      e.printStackTrace();
      Log.e(TAG, e.getMessage());
    }

    // apply click events
    applyClickEvents(holder, position, idPic.getImage_name());

  }

  //handling different click events
  private void applyClickEvents(RecyclerView.ViewHolder holder, final int position, String imageName) {

      ((ImageListViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          listener.onImageClicked(position, imageName);
        }
      });

    /*holder.iconDel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        listener.onDeleteImageClicked(position);
      }
    });*/
/*
        holder.messageContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onRowLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
        */
  }

  public void refreshImageList(final List<IdPic> newImage){
    if (this.idPics != null) {
      this.idPics.clear();
    }
      this.idPics = newImage;
      notifyDataSetChanged();

  }

  public void addNewImageToList(String filePath){
    IdPic imageModel = new IdPic();
    imageModel.setImage(filePath);
    idPics.add(imageModel);
    notifyDataSetChanged();
  }


  @Override
  public long getItemId(int position) {
    return idPics.get(position).getPic_id();
  }

  @Override
  public int getItemCount() {
    if (idPics == null) return 0;
    return idPics.size();
  }

  public class ImageListViewHolder extends RecyclerView.ViewHolder {
    ImageView image;
    public RelativeLayout imgContainer;

    public ImageListViewHolder(View itemView) {
      super(itemView);
      image = itemView.findViewById(R.id.idPicImageView);
      imgContainer = itemView.findViewById(R.id.idPicContainer);
      /*imgContainer.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          listener.onImageLongClicked(getAdapterPosition());
          view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
          return true;
        }
      });*/
      /*itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onItemClickListener.onItemClick(getAdapterPosition(), v);
        }
      });*/
    }
  }

  public interface IdPicsAdapterListener {

    void onImageClicked(int position, String imageName);

  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public interface OnItemClickListener {
    void onItemClick(int position, View v);
  }

}
