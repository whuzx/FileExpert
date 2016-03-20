package xcxin.filexpert;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class DonateDlg extends Dialog implements OnClickListener {
	
	private Activity m_context;
	public DonateDlg(Activity context) {
		super(context);
		setTitle(context.getString(R.string.donation));
		m_context = context;
		this.setContentView(R.layout.donate);
		ImageButton button = (ImageButton)this.findViewById(R.id.btn_paypal);
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		FileLister.startPaymentProcess(m_context);
		dismiss();
	}
}
