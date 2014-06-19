package com.znv.linkup.view.dialog;

import android.app.Dialog;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.znv.linkup.GameActivity;
import com.znv.linkup.R;
import com.znv.linkup.db.DbScore;
import com.znv.linkup.db.LevelScore;
import com.znv.linkup.rest.IUpload;
import com.znv.linkup.rest.UserInfo;
import com.znv.linkup.rest.UserScore;
import com.znv.linkup.util.StringUtil;
import com.znv.linkup.view.LevelTop;

/**
 * 计时模式结果
 * 
 * @author yzb
 * 
 */
public class TimeDialog extends Dialog implements IUpload {

    private GameActivity linkup = null;
    private ResultInfo resultInfo = null;
    private LevelTop levelTop = null;

    public TimeDialog(final GameActivity linkup) {
        super(linkup, R.style.CustomDialogStyle);
        this.linkup = linkup;
        setContentView(R.layout.time_dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Button btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cancel();
                linkup.onBackPressed();
            }

        });

        Button btnAgain = (Button) findViewById(R.id.btnAgain);
        btnAgain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cancel();
                linkup.start();
            }
        });

        Button btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cancel();
                linkup.next();
            }
        });

        levelTop = (LevelTop) findViewById(R.id.time_top);
        levelTop.setUploadListener(this);
    }

    /**
     * 处理返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Button btn = (Button) findViewById(R.id.btnBack);
            btn.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 显示游戏胜利对话框
     * 
     * @param resultInfo
     *            游戏结果
     */
    public void showDialog(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
        TextView tvTime = (TextView) findViewById(R.id.success_time);
        tvTime.setText(StringUtil.secondToString(resultInfo.getTime()));
        TextView tvRecord = (TextView) findViewById(R.id.time_record);
        tvRecord.setText(StringUtil.secondToString(linkup.getLevelCfg().getMaxScore()));
        ImageView ivRecord = (ImageView) findViewById(R.id.level_champion);
        ivRecord.setVisibility(View.INVISIBLE);
        if (resultInfo.isNewRecord()) {
            ivRecord.setVisibility(View.VISIBLE);
        }
        
        uploadTime();
        
        show();
    }

    /**
     * 上传时间
     */
    private void uploadTime() {
        // 判断是否已登录
        if (!resultInfo.getUserId().equals("")) {
            if (resultInfo.isNewRecord()) {
                UserScore.addTime(resultInfo.getUserId(), resultInfo.getLevel(), resultInfo.getTime(), levelTop);
            } else {
                if (!resultInfo.isUpload()) {
                    UserScore.addTime(resultInfo.getUserId(), resultInfo.getLevel(), resultInfo.getMinTime(), levelTop);
                }
            }

            // 获取排行榜
            UserScore.getTopTimes(resultInfo.getLevel(), levelTop);
        } else {
            // 没有登录则提示登录
        }
    }

    @Override
    public void onLoginSuccess(Message msg) {
        UserInfo userInfo = (UserInfo) msg.obj;
        if (userInfo != null) {
            resultInfo.setUserId(userInfo.getUserId());
            uploadTime();
        }
    }

    @Override
    public void onScoreAdd(Message msg) {
    }

    @Override
    public void onTimeAdd(Message msg) {
        // 更新是否已上传
        linkup.getLevelCfg().setUpload(true);
        LevelScore ls = new LevelScore(resultInfo.getLevel());
        ls.setIsUpload(1);
        DbScore.updateUpload(ls);
    }

    @Override
    public void onAuthorizeClick() {
        
    }
}
