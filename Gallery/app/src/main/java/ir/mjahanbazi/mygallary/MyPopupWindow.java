/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.mjahanbazi.mygallary;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;


import static ir.mjahanbazi.mygallary.GalleryUtils.activity;
import static ir.mjahanbazi.mygallary.GalleryUtils.root;


/**
 * @author najvadev
 */
public abstract class MyPopupWindow extends RelativeLayout {
    private VisibilityListener listener;

    public interface VisibilityListener {

        public void beforeShow();

        public void afterShow();

        public void beforeHide();

        public void afterHide();

    }

    public class VisibilityListenerDefault implements VisibilityListener {

        public void beforeShow() {
        }

        public void afterShow() {
        }

        public void beforeHide() {
        }

        public void afterHide() {
        }

    }

    public void setShowListener(VisibilityListener l) {
        this.listener = l;
    }

    public MyPopupWindow(Context context) {
        this(context, null);
    }

    public MyPopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT) {
            {
                setMargins(0, 0, 0, 0);
                addRule(RelativeLayout.CENTER_HORIZONTAL);
                addRule(RelativeLayout.CENTER_VERTICAL);
            }
        });
        this.setBackgroundColor(Color.TRANSPARENT);
    }

    public abstract ViewGroup getMenu();

    public boolean isTransient() {
        return true;
    }

    private static List<MyPopupWindow> dialogsList = new ArrayList<MyPopupWindow>();

    public boolean closeMe() {
        if (listener != null) {
            listener.beforeHide();
        }
        dialogsList.remove(this);
        final ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            removeKeyboard();
            parent.removeView(MyPopupWindow.this);
            if (listener != null) {
                listener.afterHide();
            }
            return true;
        }
        return false;
    }

    public synchronized static void processTransient() {
        int i = 0;
        while (i < dialogsList.size()) {
            MyPopupWindow popup = dialogsList.get(i);
            if (popup.isTransient()) {
                popup.closeMe();
            } else {
                popup.show();
                i++;
            }
        }
    }

    public void show() {
        if (this.getParent() != null) {
            ViewGroup parent = (ViewGroup) this.getParent();
            parent.removeView(this);
        }
        if (!dialogsList.contains(this)) {
            dialogsList.add(this);
        }
        if (listener != null) {
            listener.beforeShow();
        }
        final ViewGroup rootView = root;
        final ViewGroup menu = getMenu();
        rootView.addView(this);
        removeKeyboard();
        if (listener != null) {
            listener.afterShow();
        }
    }

    protected boolean backProcessor() {
        return closeMe();
    }

    public static boolean popupBackProcessor() {
        if (dialogsList.isEmpty()) {
            return false;
        }
        MyPopupWindow lastDialog = dialogsList.get(dialogsList.size() - 1);
        return lastDialog.backProcessor();
    }

    private void removeKeyboard() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            final View currentFocus = activity.getCurrentFocus();
            if (currentFocus != null) {
                currentFocus.clearFocus();
                imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
            }
        }
    }
}
