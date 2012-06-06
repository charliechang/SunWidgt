package com.bk.sunwidgt.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

public class ProgressFragment extends DialogFragment{
    
    private final static String RESID = ProgressFragment.class.getName() + ".resid";
    private String m_msg;
    public static ProgressFragment newInstance(int resid) {
        final ProgressFragment dialog = new ProgressFragment();
        Bundle b = new Bundle();
        b.putInt(RESID, resid);
        dialog.setArguments(b);
        return dialog;
    }
    
    public void setMessage(String msg) {
        m_msg = msg;
        if(getDialog() != null) {
            final ProgressDialog progDialog = (ProgressDialog) getDialog();
            progDialog.setMessage(m_msg);
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        final int resid = getArguments().getInt(RESID);
        final ProgressDialog progDialog = new ProgressDialog(getActivity());
        
        if(m_msg != null) {
            progDialog.setMessage(m_msg);
        }
        else {
            progDialog.setMessage(getActivity().getResources().getString(resid));
        }
        setCancelable(false);
        
        return progDialog;
        
    }
}
