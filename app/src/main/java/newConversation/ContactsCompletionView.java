package newConversation;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioane.sharvadze.geosms.R;
import com.ioane.sharvadze.geosms.objects.Contact;
import com.tokenautocomplete.TokenCompleteTextView;

import utils.Utils;

/**
 * Class representing bubble
 *
 * Created by Ioane on 4/14/2015.
 */
public class ContactsCompletionView extends TokenCompleteTextView {
    public ContactsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(Object object) {
        Contact contact = (Contact)object;
        String name;
        String phoneNumber;
        String photoUri;
        if(contact == null){
            name = null;
            phoneNumber = "no phone";
            photoUri = null;
        }else{
            name = contact.getName();
            phoneNumber = contact.getAddress();
            photoUri = contact.getPhotoUri();
        }

        LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout)l.inflate(R.layout.contact_bubble, (ViewGroup)ContactsCompletionView.this.getParent(), false);

        TextView nameView = ((TextView)view.findViewById(R.id.name));
        TextView numberView = ((TextView)view.findViewById(R.id.number));
        numberView.setText(phoneNumber);

        if(name != null)
            nameView.setText(name);
        else{
            nameView.setVisibility(GONE);
            numberView.setTextSize(18);
        }

        if(photoUri != null){
            Bitmap bitmap = Utils.getCircleBitmap(Utils.getPhotoFromURI(photoUri, getContext(), 40));
            ((ImageView)view.findViewById(R.id.contact_picture_image_view)).setImageBitmap(bitmap);
        }
        view.setTag(object);
        return view;
    }

    @Override
    protected Object defaultObject(String completionText) {
        //Stupid simple example of guessing if we have an email or not
        return new Contact(-1,null,null,completionText,null);
    }
}