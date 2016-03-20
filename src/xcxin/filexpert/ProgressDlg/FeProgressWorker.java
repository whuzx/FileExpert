package xcxin.filexpert.ProgressDlg;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

abstract public class FeProgressWorker extends Object {

	private Handler mHandler;
	private AtomicInteger ai;

	public static final int RUN_STATE = 0;
	public static final int WAIT_STATE = 1;

	public FeProgressWorker(Handler handler) {
		ai = new AtomicInteger(RUN_STATE);
		attachHandler(handler);
	}

	public FeProgressWorker() {
		ai = new AtomicInteger(RUN_STATE);
	}

	public void attachHandler(Handler handler) {
		mHandler = handler;
	}

	public Handler getHandler() {
		return mHandler;
	}

	public void updateProgressText(String text) {
		setState(WAIT_STATE);
		sendMsg(text);
		waitUntilFinish();
	}

	public void updateProgressValue(int value) {
		setState(WAIT_STATE);
		sendValue(value);
		waitUntilFinish();
	}

	public void updateProgressMax(int size) {
		setState(WAIT_STATE);
		sendItemsSize(size);
		waitUntilFinish();
	}

	public void updateView() {
		setState(WAIT_STATE);
		sendUpdateView();
		waitUntilFinish();
	}

	public void updateToastMessage(String msg) {
		setState(WAIT_STATE);
		sendToastMessage(msg);
		waitUntilFinish();
	}

	protected void sendMsg(String text) {
		Bundle bdl = new Bundle();
		bdl.putString(FeProgressDialog.UPDATE_MSG, text);
		Message msg = mHandler.obtainMessage();
		msg.setData(bdl);
		mHandler.sendMessage(msg);
	}

	protected void sendValue(int value) {
		Bundle bdl = new Bundle();
		bdl.putString(FeProgressDialog.UPDATE_VALUE, Integer.toString(value));
		Message msg = mHandler.obtainMessage();
		msg.setData(bdl);
		mHandler.sendMessage(msg);
	}

	protected void sendItemsSize(int size) {
		Bundle bdl = new Bundle();
		bdl.putString(FeProgressDialog.UPDATE_SIZE, Integer.toString(size));
		Message msg = mHandler.obtainMessage();
		msg.setData(bdl);
		mHandler.sendMessage(msg);
	}

	protected void sendUpdateView() {
		Bundle bdl = new Bundle();
		bdl.putString(FeProgressDialog.UPDATE_VIEW, " ");
		Message msg = mHandler.obtainMessage();
		msg.setData(bdl);
		mHandler.sendMessage(msg);
	}
	
	protected void sendToastMessage(String tmsg) {
		Bundle bdl = new Bundle();
		bdl.putString(FeProgressDialog.TOAST_MESSAGE, tmsg);
		Message msg = mHandler.obtainMessage();
		msg.setData(bdl);
		mHandler.sendMessage(msg);
	}

	public void setState(int state) {
		ai.set(state);
	}

	public void waitUntilFinish() {
		while (ai.get() != RUN_STATE)
			;
	}

	public void resumeExecution() {
		setState(RUN_STATE);
	}

	abstract public void work(Context context);

	abstract public void onCancel();

	abstract public void onFinish();

	abstract public void onBackgroud();
}
