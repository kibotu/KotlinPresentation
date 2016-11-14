package net.kibotu.base.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.common.android.utils.interfaces.LayoutProvider;
import com.common.android.utils.interfaces.LogTag;
import com.common.android.utils.misc.UIDGenerator;

import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static com.common.android.utils.ContextHelper.getFragmentActivity;

/**
 * Created by jan.rabe on 26/08/16.
 * <img src="https://raw.githubusercontent.com/Aracem/android-lifecycle/master/complete_android_fragment_lifecycle.png"/>
 */

public abstract class BaseDialogFragment extends DialogFragment implements LayoutProvider, LogTag {

    private Unbinder unbinder;

    protected final int uid = UIDGenerator.newUID();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        final Window window = dialog.getWindow();
        addFlags(window);
        return dialog;
    }

    private static void addFlags(@Nullable Window window) {
        if (window == null)
            return;

        if (SDK_INT >= LOLLIPOP)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        if (SDK_INT >= KITKAT)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        if (SDK_INT >= LOLLIPOP_MR1) {
            window.setClipToOutline(false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public BaseDialogFragment show() {
        if (getFragmentActivity().getSupportFragmentManager().findFragmentByTag(getClass().getCanonicalName()) != null)
            return this;

        show(getFragmentActivity().getSupportFragmentManager(), getClass().getCanonicalName());
        return this;
    }

    @NonNull
    @Override
    public String tag() {
        return getClass().getSimpleName() + "[" + uid + "]";
    }
}