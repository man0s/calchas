package ceid.katefidis.calchas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class ConsentActivity extends Activity {

    private CheckBox understand;
    private CheckBox agree;
    private Button ok_button;
    private int return_code = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);
//        TextView tv = (TextView) findViewById(R.id.welcome);
//        Typeface face = Typeface.createFromAsset(getAssets(),
//                "fonts/Lobster.ttf");
//        tv.setTypeface(face);

        understand = (CheckBox) findViewById(R.id.checkBox1);
        agree = (CheckBox) findViewById(R.id.checkBox2);
        ok_button = (Button) findViewById(R.id.cons_button_ok);
        agree.setEnabled(false);
        ok_button.setEnabled(false);

        understand.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if(isChecked)
                {
                    agree.setEnabled(true);
                    ok_button.setEnabled(true);
                    //agree.setTextColor(getResources().getColor(R.attr.colorText));
                }
                else
                {
                    agree.setEnabled(false);
                    ok_button.setEnabled(false);
                    //agree.setTextColor(getResources().getColor(R.attr.colorTextLight));
                }

            }
        });

        ok_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                boolean und = understand.isChecked();
                boolean agr = agree.isChecked();

                if (und)
                {
                    if(agr)
                        return_code=RESULT_OK;
                    else
                        return_code=RESULT_CANCELED;
                }
                else
                    return_code=RESULT_CANCELED;

                //go back to Calchas
                Intent calchas = new Intent();
                setResult(return_code, calchas);
                //---close the activity---
                finish();
            }
        });
    }

}

