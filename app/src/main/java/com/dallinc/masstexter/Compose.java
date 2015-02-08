package com.dallinc.masstexter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;

import java.util.ArrayList;
import java.util.Iterator;

import contactpicker.Contact;
import contactpicker.ContactManager;
import contactpicker.FlowLayout;


public class Compose extends ActionBarActivity {
    final int REQUEST_CODE = 100;
    boolean repeatCheck = false;
    int i = 0;
    ArrayList<Contact> contactsShareDetail;
    ArrayList<String> contactsSharePhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        ButtonRectangle sendMessageBtn = (ButtonRectangle) findViewById(R.id.sendMessage);
        sendMessageBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        contactsShareDetail = new ArrayList<Contact>();
        contactsSharePhone = new ArrayList<String>();
        ScrollViewWithMaxHeight maxHeightScrollView = (ScrollViewWithMaxHeight) findViewById(R.id.maxHeightScrollView);
        maxHeightScrollView.setMaxHeight(180);

        FloatingLabelEditText editText = (FloatingLabelEditText) findViewById(R.id.composeBody);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            final Template template = Template.findById(Template.class, bundle.getLong("template_id"));
            template.buildArrayListFromString();
            editText.setInputWidgetText(template.body);
            styleEditText(template.variables);

            editText.addInputWidgetTextChangedListener(new TextWatcher() {
                String before_text = "";
                int before_variable_count = 0;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    before_text = s.toString();
                    before_variable_count = variableInstances(s.toString(), start+count);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int after_variable_count = variableInstances(s.toString(), start+count);
                    while(after_variable_count < before_variable_count) {
                        // Remove the appropriate variable(s)
                        template.variables.remove(before_variable_count - 1);
                        before_variable_count--;
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    private int variableInstances(String s, int end_position) {
        int count = 0;
        for(int i=0; i<end_position; i++ ) {
            if(s.charAt(i) == '¬') {
                count++;
            }
        }
        return count;
    }

    private void styleEditText(ArrayList<String> variables) {
        FloatingLabelEditText bodyInputField = (FloatingLabelEditText) findViewById(R.id.composeBody);
        String template_text = bodyInputField.getInputWidgetText().toString();
        SpannableString spanText = new SpannableString(template_text);

        int starting_pos = 0;
        int variable_idx = 0;
        while(starting_pos != -1) {
            int idx = template_text.indexOf("¬", starting_pos);
            if(idx == -1) {
                break;
            }

            String variable = variables.get(variable_idx);

            Rect bounds = new Rect();
            Paint textPaint = bodyInputField.getInputWidget().getPaint();
            textPaint.getTextBounds(variable, 0, variable.length(), bounds);
            int width = bounds.width();

            TextDrawable d = new TextDrawable(this);
            d.setText(variable);
            d.setTextColor(getResources().getColor(R.color.colorAccent));
            d.setTextSize(20);
            d.setTextAlign(Layout.Alignment.ALIGN_CENTER);
            d.setBounds(3, 0, width+6, (int)(bodyInputField.getInputWidget().getTextSize()));

            spanText.setSpan(new ImageSpan(d, ImageSpan.ALIGN_BASELINE), idx, idx+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            starting_pos = idx+1;
            variable_idx++;
        }

        bodyInputField.setInputWidgetText(spanText, TextView.BufferType.SPANNABLE);
        try{
            bodyInputField.getInputWidget().setSelection(bodyInputField.getInputWidget().getSelectionEnd());
        } catch(IndexOutOfBoundsException e) {
            bodyInputField.getInputWidget().setSelection(bodyInputField.getInputWidgetText().length());
        }

    }

    public void clear() {
        contactsShareDetail = new ArrayList<Contact>();
        contactsSharePhone = new ArrayList<String>();

        FlowLayout chipsBoxLayout = (FlowLayout)findViewById(R.id.chips_box_layout);
        chipsBoxLayout.removeAllViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_select_recipients) {
            clear();
            Intent contactPicker = new Intent(getBaseContext(), ContactManager.class);
            startActivityForResult(contactPicker, REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                if(data.hasExtra(Contact.CONTACTS_DATA)) {

                    FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(2, 2, 2, 2);

                    ArrayList<Contact> contacts = data.getParcelableArrayListExtra(Contact.CONTACTS_DATA);
                    if(contacts != null) {
                        i += 1;
                        if (i == 2) {
                            repeatCheck = true;
                        }

                        Iterator<Contact> iterContacts = contacts.iterator();
                        while (iterContacts.hasNext()) {
                            Contact contact = iterContacts.next();
                            if(!repeatCheck)
                            {
                                contactsShareDetail.add(contact);
                                contactsSharePhone.add(contact.getContactNumber());
                                TextView t = new TextView(getBaseContext());
                                t.setLayoutParams(params);
                                t.setTextSize(16f);
                                t.setPadding(3, 3, 3, 3);
                                t.setText(contact.getContactName());
                                t.setTextColor(Color.WHITE);
                                // t.setBackgroundColor(Color.BLUE);
                                t.setBackgroundResource(R.drawable.chips_bg);

                                FlowLayout chipsBoxLayout = (FlowLayout)findViewById(R.id.chips_box_layout);
                                chipsBoxLayout.addView(t);
                            }
                            else if(repeatCheck && !contactsSharePhone.contains(contact.getContactNumber()))
                            {
                                contactsShareDetail.add(contact);
                                contactsSharePhone.add(contact.getContactNumber());
                                TextView t = new TextView(getBaseContext());
                                t.setLayoutParams(params);
                                t.setTextSize(16f);
                                t.setPadding(3, 3, 3, 3);
                                t.setText(contact.getContactName());
                                t.setTextColor(Color.WHITE);
                                // t.setBackgroundColor(Color.BLUE);
                                t.setBackgroundResource(R.drawable.chips_bg);

                                FlowLayout chipsBoxLayout = (FlowLayout)findViewById(R.id.chips_box_layout);
                                chipsBoxLayout.addView(t);
                            }

                        }
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}