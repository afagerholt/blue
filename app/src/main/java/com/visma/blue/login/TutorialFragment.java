package com.visma.blue.login;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.visma.blue.R;

public final class TutorialFragment extends Fragment {
    private static final String KEY_CONTENT = "TutorialFragment:Content";

    private static int[] mTitleIds = new int[]{
            R.string.visma_blue_login_tutorial_page_1_title,
            R.string.visma_blue_login_tutorial_page_2_title,
            R.string.visma_blue_login_tutorial_page_3_title};

    private static int[] mTextIds = new int[]{
            R.string.visma_blue_login_tutorial_page_1_text,
            R.string.visma_blue_login_tutorial_page_2_text,
            R.string.visma_blue_login_tutorial_page_3_text};

    private static int[] mImageIds = new int[]{
            R.drawable.blue_login_tutorial_step1,
            R.drawable.blue_login_tutorial_step2,
            R.drawable.blue_login_tutorial_step3};

    private int mContent;

    public static TutorialFragment newInstance(int content) {
        TutorialFragment fragment = new TutorialFragment();
        fragment.mContent = content;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getInt(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.blue_fragment_tutorial_page, container, false);

        ImageView image = (ImageView) view.findViewById(R.id.blue_fragment_tutorial_image);
        image.setImageResource(mImageIds[mContent]);

        TextView title = (TextView) view.findViewById(R.id.blue_fragment_tutorial_title);
        title.setText(mTitleIds[mContent]);

        TextView text = (TextView) view.findViewById(R.id.blue_fragment_tutorial_text);
        text.setText(mTextIds[mContent]);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTENT, mContent);
    }
}
