package com.tepia.guangdong_module.amainguangdong.xunchaview.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.guangdong_module.R;
import com.example.guangdong_module.databinding.ActivityDangerReportBinding;
import com.example.guangdong_module.databinding.ActivityTroubleReportBinding;
import com.google.gson.Gson;
import com.tepia.base.CacheConsts;
import com.tepia.base.ConfigConsts;
import com.tepia.base.mvp.BaseActivity;
import com.tepia.base.utils.TimeFormatUtils;
import com.tepia.base.utils.ToastUtils;
import com.tepia.base.view.dialog.basedailog.ActionSheetDialog;
import com.tepia.base.view.dialog.basedailog.OnOpenItemClick;
import com.tepia.base.view.floatview.CollectionsUtil;
import com.tepia.guangdong_module.amainguangdong.common.PhotoSelectAdapter;
import com.tepia.guangdong_module.amainguangdong.common.UserManager;
import com.tepia.guangdong_module.amainguangdong.common.pickview.OnItemClickListener;
import com.tepia.guangdong_module.amainguangdong.common.pickview.PhotoRecycleViewAdapter;
import com.tepia.guangdong_module.amainguangdong.model.UtilDataBaseOfGD;
import com.tepia.guangdong_module.amainguangdong.model.xuncha.DataBeanOflistReservoirRoute;
import com.tepia.guangdong_module.amainguangdong.model.xuncha.PersonDutyBean;
import com.tepia.guangdong_module.amainguangdong.model.xuncha.ReservoirBean;
import com.tepia.guangdong_module.amainguangdong.route.TaskBean;
import com.tepia.guangdong_module.amainguangdong.route.TaskItemBean;
import com.tepia.guangdong_module.amainguangdong.utils.EmptyLayoutUtil;
import com.tepia.guangdong_module.amainguangdong.xunchaview.adapter.AdapterWorker;
import com.tepia.photo_picker.PhotoPicker;
import com.tepia.photo_picker.PhotoPreview;
import com.tepia.photo_picker.entity.CheckTaskPicturesBean;
import com.tepia.photo_picker.utils.SPUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by      Android studio
 *
 * @author :ly (from Center Of Wuhan)
 * 创建时间 :2019-5-5
 * 更新时间 :
 * Version :1.0
 * 功能描述 :隐患记录
 **/
public class TroubleRecordActivity extends BaseActivity {

    ActivityTroubleReportBinding mBinding;
    AdapterWorker adapterWorker;

    private PhotoSelectAdapter photoRecycleViewAdapterBefore;
    public ArrayList<String> selectPhotosBefore = new ArrayList<>();

    private TaskItemBean taskItemBean;
    private ReservoirBean reservoirBean;

    String userCode;
    String reservoirId;
    /**
     * 唯一id,通过UUIDUtil.getUUID32() 生成
     */
    String itemId = "1";
    String[] items = new String[]{"坝体", "坝脚", "泄水设施", "输水设施", "其他"};
    DataBeanOflistReservoirRoute offlineDataBean;
    private List<PersonDutyBean> personDutyBeanList = new ArrayList<>();
    String executeResultType = "";

    boolean isCompleteOfTaskBean;


    @Override
    public int getLayoutId() {
        return R.layout.activity_trouble_report;
    }

    @Override
    public void initData() {
//        ToastUtils.shortToast("请填写异常信息，否则该项巡查将视为未完成");
        executeResultType = getIntent().getStringExtra("executeResultType");
        mBinding = DataBindingUtil.bind(mRootView);

        mBinding.layoutDes.titleTv.setText("填写异常情况描述");
        mBinding.layoutDes.yichangTitleTv.setText("异常情况");
        mBinding.layoutDes.positionNameLy.setVisibility(View.GONE);
        /**
         * 需要上一页面传过来itemId，然后通过itemId去数据库中查找
         */
        itemId = getIntent().getStringExtra("item");

//        itemId = UUIDUtil.getUUID32();
        /**
         * 险情图片类型约定
         */
        SPUtils.getInstance().putString(CacheConsts.bizType, ConfigConsts.picType_trouble);
        SPUtils.getInstance().putString(CacheConsts.itemId, itemId);
        reservoirBean = UserManager.getInstance().getDefaultReservoir();
//        userCode = SPUtils.getInstance().getString(CacheConsts.userCode, "");
        reservoirId = SPUtils.getInstance().getString(CacheConsts.reservoirId, "");
        taskItemBean = DataSupport.where("reservoirId=? and itemId=?", reservoirId, itemId).findFirst(TaskItemBean.class);
        userCode = taskItemBean.getUserCode();
        isCompleteOfTaskBean = stopClick(taskItemBean.getWorkOrderId());
        if (isCompleteOfTaskBean) {
            mBinding.reportTv.setVisibility(View.GONE);
            mBinding.layoutDes.desEt.setEnabled(false);
            mBinding.layoutTrouble.copyBtn.setVisibility(View.INVISIBLE);
        }

        mBinding.layoutTrouble.operationLevelTv.setVisibility(View.GONE);
        String operationLevel = taskItemBean.getOperationLevel();
        if ("1".equals(operationLevel)) {
            //一般项
            mBinding.layoutTrouble.operationLevelTv.setVisibility(View.VISIBLE);
            mBinding.layoutTrouble.operationLevelTv.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.color_load_yellow));
            mBinding.layoutTrouble.operationLevelTv.setText("一般项");
        } else if ("2".equals(operationLevel)) {
            //报警项
            mBinding.layoutTrouble.operationLevelTv.setVisibility(View.VISIBLE);
            mBinding.layoutTrouble.operationLevelTv.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.common_red));
            mBinding.layoutTrouble.operationLevelTv.setText("报警项");
        }

        mBinding.loHeader.tvReservoirName.setText(reservoirBean.getReservoir());

    }

    private boolean stopClick(String workOrderId) {
        TaskBean taskBean = DataSupport.where("workOrderId=?", workOrderId).findFirst(TaskBean.class);
        if (taskBean != null) {
            if ("3".equals(taskBean.getExecuteStatus())) {
                return true;
            }
        }
        return false;

    }

    @Override
    public void initView() {
        setCenterTitle("隐患记录");
        showBack();
        mBinding.layoutTel.rootTelLy.setVisibility(View.GONE);
        mBinding.reportTv.setText("提交");
        mBinding.layoutDes.tianqiDesTv.setVisibility(View.GONE);

        initRec();
        initPhotoListView();
        /*mBinding.layoutDes.selectTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });*/

        if (taskItemBean != null) {
            mBinding.layoutDes.selectTv.setText(taskItemBean.getPositionName());
            mBinding.layoutDes.desEt.setText(taskItemBean.getExecuteResultDescription());
        }
        mBinding.layoutTrouble.contentTv.setText(taskItemBean.getSuperviseItemContent());

    }

    /**
     * 替换字符串中特殊字符
     *
     * @param s
     * @return
     */
    private String format(String s) {
        return s.replaceAll("[1234567890`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……& amp;*（）——+|{}【】‘；：”“’。，、？|-]", "");
    }

    @Override
    protected void initListener() {
        mBinding.layoutTrouble.copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取剪贴版
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                //创建ClipData对象
                //第一个参数只是一个标记，随便传入。
                //第二个参数是要复制到剪贴版的内容
                // 获取edittext中输入的参数
                String tv_content = mBinding.layoutTrouble.contentTv.getText().toString();
                StringBuilder builder = new StringBuilder();
                // 截取字符串前6位替换标点符号和字符等
                if (!TextUtils.isEmpty(tv_content) && tv_content.length() > 6) {
                    builder.append(format(tv_content.substring(0, 6)));
                    builder.append(tv_content.substring(6));
                } else {
                    builder.append(format(tv_content));
                }
//                ClipData clip = ClipData.newPlainText("simple text", builder.toString());
//                //传入clipdata对象.
//                clipboard.setPrimaryClip(clip);
//                ToastUtils.shortToast("已复制");
                String et_content = mBinding.layoutDes.desEt.getText().toString();
                builder.append(et_content);
                mBinding.layoutDes.desEt.setText(builder.toString());
            }
        });
        mBinding.reportTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String positionName = mBinding.layoutDes.selectTv.getText().toString();
                String problemDescription = mBinding.layoutDes.desEt.getText().toString();
                /*if (TextUtils.isEmpty(positionName) && items.length > 0) {
                    ToastUtils.shortToast("请选择险情部位");
                    return;
                }*/

                if (TextUtils.isEmpty(problemDescription)) {
                    ToastUtils.shortToast("请填写异常信息");
                    return;
                }

                if (CollectionsUtil.isEmpty(selectPhotosBefore)) {
                    ToastUtils.shortToast("请上传图片");
                    return;
                }

                if (taskItemBean != null) {
                    taskItemBean.setBeforelist(new Gson().toJson(selectPhotosBefore));

                    taskItemBean.setPositionName(positionName);
                    taskItemBean.setExecuteResultDescription(problemDescription);
                    taskItemBean.setCompleteStatus("1");

                    if (!TextUtils.isEmpty(executeResultType)) {
                        taskItemBean.setExecuteResultType(executeResultType);
                    }
                    taskItemBean.setExcuteDate(TimeFormatUtils.getStringDate());

                    taskItemBean.updateAll("userCode=? and reservoirId=? and itemId=?", userCode, reservoirId, itemId);
                }

                ToastUtils.shortToast("提交成功");
                Intent intent = new Intent();

                TroubleRecordActivity.this.setResult(1000, intent);

                finish();


            }
        });
    }

    @Override
    protected void initRequestData() {

    }

    private void initRec() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBinding.layoutTel.rvWorker.setLayoutManager(layoutManager);
        adapterWorker = new AdapterWorker(R.layout.lv_tab_main_worker_item, personDutyBeanList);
        mBinding.layoutTel.rvWorker.setAdapter(adapterWorker);
        mBinding.layoutTel.rvWorker.setNestedScrollingEnabled(false);
        if (CollectionsUtil.isEmpty(personDutyBeanList)) {
            adapterWorker.setEmptyView(EmptyLayoutUtil.showNew("暂无数据"));
        }
        adapterWorker.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!TextUtils.isEmpty(adapterWorker.getData().get(position).getMobile())) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    Uri data = Uri.parse("tel:" + adapterWorker.getData().get(position).getMobile());
                    intent.setData(data);
                    startActivity(intent);
                } else {
                    ToastUtils.shortToast("暂无手机号码");
                }
            }

        });
    }

    /**
     * 初始化 图片选择
     */
    List<CheckTaskPicturesBean> checkTaskPicturesBeanList = new ArrayList<>();

    private void initPhotoListView() {
        {
            photoRecycleViewAdapterBefore = new PhotoSelectAdapter(getContext(), isCompleteOfTaskBean);
            mBinding.layoutPic.rvAddPhotoBefore.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
            mBinding.layoutPic.rvAddPhotoBefore.setAdapter(photoRecycleViewAdapterBefore);

            selectPhotosBefore.clear();
            checkTaskPicturesBeanList = UtilDataBaseOfGD.getInstance().getCheckTaskPicturesBeanOfTrouble(ConfigConsts.picType_trouble, userCode, reservoirId, itemId);
            for (CheckTaskPicturesBean checkTaskPicturesBean : checkTaskPicturesBeanList) {
                if (checkTaskPicturesBean != null) {
                    selectPhotosBefore.add(checkTaskPicturesBean.getFilePath());
                }
            }
            photoRecycleViewAdapterBefore.setLocalData(selectPhotosBefore);
            mBinding.layoutPic.tvPhotoNumBefore.setText(photoRecycleViewAdapterBefore.getPhotoPaths().size() + "/5");
            photoRecycleViewAdapterBefore.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (photoRecycleViewAdapterBefore.getItemViewType(position) == PhotoRecycleViewAdapter.TYPE_ADD) {

                        if (!isCompleteOfTaskBean) {
                            PhotoPicker.builder()
                                    .setPhotoCount(5)
                                    .setShowCamera(true)
                                    .setPreviewEnabled(true)
                                    .setSelected(selectPhotosBefore)
                                    .start(TroubleRecordActivity.this, 100);
                        }

                    } else {
                        PhotoPreview.builder()
                                .setPhotos(photoRecycleViewAdapterBefore.getPhotoPaths())
                                .setCurrentItem(position)
                                .setShowDeleteButton(false)
                                .start(TroubleRecordActivity.this, 101);
                    }
                }
            });

            if (!isCompleteOfTaskBean) {
                photoRecycleViewAdapterBefore.setDeleteListener(new PhotoSelectAdapter.DeleteListener() {

                    @Override
                    public void ondelete(int position) {
                        if (selectPhotosBefore.size() > 0 && selectPhotosBefore.size() > position) {
                            String bizType = SPUtils.getInstance().getString(CacheConsts.bizType, "");
                            String userCode = SPUtils.getInstance().getString(CacheConsts.userCode, "");
                            String reservoirId = SPUtils.getInstance().getString(CacheConsts.reservoirId, "");
                            UtilDataBaseOfGD.getInstance().deletePic(bizType, selectPhotosBefore.get(position), userCode, reservoirId);
                            selectPhotosBefore.remove(position);

                        }

                        photoRecycleViewAdapterBefore.setLocalData(selectPhotosBefore);
                        mBinding.layoutPic.tvPhotoNumBefore.setText(photoRecycleViewAdapterBefore.getPhotoPaths().size() + "/5");

                    }
                });
            } else {
//                photoRecycleViewAdapterBefore
            }


        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            List<String> photos = null;
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }

            if (photos != null) {
                checkTaskPicturesBeanList = UtilDataBaseOfGD.getInstance().getCheckTaskPicturesBeanOfTrouble(ConfigConsts.picType_trouble, userCode, reservoirId, itemId);
                selectPhotosBefore.clear();

                for (CheckTaskPicturesBean checkTaskPicturesBean : checkTaskPicturesBeanList) {
                    if (checkTaskPicturesBean != null) {
                        selectPhotosBefore.add(checkTaskPicturesBean.getFilePath());
                    }
                }
                photoRecycleViewAdapterBefore.setLocalData(selectPhotosBefore);
                mBinding.layoutPic.tvPhotoNumBefore.setText(photoRecycleViewAdapterBefore.getPhotoPaths().size() + "/5");

            }

        }


    }

    /**
     * 框
     */

    private void showDialog() {
        /*ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles(items)
                .setCancelableOnTouchOutside(true)
                .setListener(new ActionSheet.ActionSheetListener() {
                    @Override
                    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

                    }

                    @Override
                    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
                        mBinding.layoutDes.selectTv.setText(items[index]);
                    }
                })
                .show();*/


        ActionSheetDialog dialog = new ActionSheetDialog(this, items, null);
        dialog.title("请选择部位")
                .titleTextSize_SP(14.5f)
                .widthScale(0.8f)
                .show();

        dialog.setOnOpenItemClickL(new OnOpenItemClick() {
            @Override
            public void onOpenItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBinding.layoutDes.selectTv.setText(items[position]);
                dialog.dismiss();
            }
        });
    }


}
