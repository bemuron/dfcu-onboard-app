package com.emetchint.dfcuonboard.presentation.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.emetchint.dfcuonboard.R;
import com.emetchint.dfcuonboard.helpers.CircleTransform;
import com.emetchint.dfcuonboard.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class EmployeeListAdapter extends RecyclerView.Adapter<EmployeeListAdapter.MyViewHolder> {

  private List<User> employeeList;
  private LayoutInflater inflater;
  Context context;
  private EmployeeListAdapterListener listener;
  private SparseBooleanArray selectedItems;

  // array used to perform multiple animation at once
  private SparseBooleanArray animationItemsIndex;
  private boolean reverseAllAnimations = false;

  // index is used to animate only the selected row
  // dirty fix, find a better solution
  private static int currentSelectedIndex = -1;

  public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
    public TextView employee_name, employee_id, employee_dob, employee_email,
            employee_reg_date, emp_role, iconText;
    public ImageView imgProfile;
    public LinearLayout empContainer;
    public RelativeLayout iconContainer, iconBack, iconFront;

    public MyViewHolder(View view) {
      super(view);
      employee_name = view.findViewById(R.id.emp_list_emp_name_tv);
      employee_dob = view.findViewById(R.id.emp_list_dob_tv);
      employee_id = view.findViewById(R.id.emp_list_emp_id_tv);
      employee_email = view.findViewById(R.id.emp_list_email_tv);
      employee_reg_date = view.findViewById(R.id.emp_list_reg_date);
      emp_role = view.findViewById(R.id.emp_list_emp_role);
      //iconImp = view.findViewById(R.id.icon_star);
      imgProfile = view.findViewById(R.id.icon_profile);
      iconText = view.findViewById(R.id.icon_text);
      empContainer = view.findViewById(R.id.employee_list_container);
      iconContainer = view.findViewById(R.id.icon_container);
      view.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View view) {
      view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
      return true;
    }
  }

  public EmployeeListAdapter(Context context, List<User> jobs, EmployeeListAdapterListener listener) {
    inflater = LayoutInflater.from(context);
    this.context = context;
    this.employeeList = jobs;
    this.listener = listener;
    selectedItems = new SparseBooleanArray();
    animationItemsIndex = new SparseBooleanArray();
  }

  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.employee_list_item, parent, false);

    return new MyViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(final MyViewHolder holder, int position) {
    User employee = employeeList.get(position);
    int empRole;
    String empDob = null;
    String regDate = null;

    //make these texts bold
    SpannableStringBuilder empId = new SpannableStringBuilder("ID: ");
    SpannableStringBuilder email = new SpannableStringBuilder("Email: ");
    SpannableStringBuilder dob = new SpannableStringBuilder("DOB: ");
    SpannableStringBuilder registeredOn = new SpannableStringBuilder("Registered On: ");

    empId.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, empId.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    email.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, email.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    dob.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, dob.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    registeredOn.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, registeredOn.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    //date format for dates coming from server
    SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    SimpleDateFormat mysqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    //the format we want them in
    SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);

    try{
      Date d = mysqlDateFormat.parse(employee.getDate_of_birth());
      d.setTime(d.getTime());
      empDob = myFormat.format(d);

      Date d2 = mysqlDateTimeFormat.parse(employee.getCreated_at());
      d2.setTime(d2.getTime());
      regDate = myFormat.format(d2);
    }catch (Exception e){
      e.printStackTrace();
    }

    // displaying text view data
    holder.employee_name.setText(employee.getName());
    holder.employee_dob.setText(dob + empDob);
    holder.employee_email.setText(email + employee.getEmail());
    holder.employee_id.setText(empId + String.valueOf(employee.getEmp_id()));
    holder.employee_reg_date.setText(registeredOn + regDate);

    // 0 - staff, 1 - admin
    empRole = employee.getRole();
    switch (empRole){
      case 0:
        holder.emp_role.setText("Staff");
        holder.emp_role.setTextColor(context.getResources().getColor(R.color.colorAccent));
        break;
      case 1:
        holder.emp_role.setText("Admin");
        holder.emp_role.setTextColor(context.getResources().getColor(R.color.darkPrimary));
        break;
    }

    // change the row state to activated
    holder.itemView.setActivated(selectedItems.get(position, false));

    // apply click events
    applyClickEvents(holder, position);

    // display profile image
    applyProfilePicture(holder, employee);

  }

  //handling different click events
  private void applyClickEvents(MyViewHolder holder, final int position) {
    holder.empContainer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        listener.onEmployeeRowClicked(position);
      }
    });
  }

  private void applyProfilePicture(MyViewHolder holder, User employee) {
    if (!TextUtils.isEmpty(employee.getProfile_pic())) {
      Glide.with(context).load("https://fixappug.com/public/assets/images/profile_pics/"+employee.getProfile_pic())
              .thumbnail(0.5f)
              .transition(withCrossFade())
              .apply(new RequestOptions().fitCenter()
                      .transform(new CircleTransform(context)).diskCacheStrategy(DiskCacheStrategy.ALL))
              .into(holder.imgProfile);
      holder.imgProfile.setColorFilter(null);
      holder.iconText.setVisibility(View.INVISIBLE);
    } else {
      holder.iconText.setVisibility(View.VISIBLE);
      holder.imgProfile.setImageResource(R.drawable.bg_circle);
      holder.iconText.setText(employee.getName().substring(0, 1));
      holder.iconText.setVisibility(View.VISIBLE);
    }
  }

  public void setList(List<User> empList) {
    this.employeeList = empList;
    notifyDataSetChanged();
  }

  @Override
  public long getItemId(int position) {
    return employeeList.get(position).getUser_id();
  }

  @Override
  public int getItemCount() {
    return employeeList.size();
  }

  public void toggleSelection(int pos) {
    currentSelectedIndex = pos;
    if (selectedItems.get(pos, false)) {
      selectedItems.delete(pos);
      animationItemsIndex.delete(pos);
    } else {
      selectedItems.put(pos, true);
      animationItemsIndex.put(pos, true);
    }
    notifyItemChanged(pos);
  }

  public void clearSelections() {
    reverseAllAnimations = true;
    selectedItems.clear();
    notifyDataSetChanged();
  }

  public int getSelectedItemCount() {
    return selectedItems.size();
  }

  public List<Integer> getSelectedItems() {
    List<Integer> items =
            new ArrayList<>(selectedItems.size());
    for (int i = 0; i < selectedItems.size(); i++) {
      items.add(selectedItems.keyAt(i));
    }
    return items;
  }

  public void removeData(int position) {
    employeeList.remove(position);
    resetCurrentIndex();
  }

  private void resetCurrentIndex() {
    currentSelectedIndex = -1;
  }

  public interface EmployeeListAdapterListener {

    void onEmployeeRowClicked(int position);

  }

}
