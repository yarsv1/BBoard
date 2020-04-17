package com.iark.bboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class IME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private enum LAYOUT {
        ENG,
        UKR,
        RUS
    }

    private KeyboardView kv;
    private Keyboard kb;
    private boolean caps = false;
    private boolean fakecaps = true;
    private boolean sym = false;
    private boolean sym2 = false;
    private LAYOUT cl;

    private void toCaps() {
        caps = !caps;
        kb.setShifted(caps);
        kv.invalidateAllKeys();
    }

    private void rl() {
        switch (cl) {
            case ENG:
                kb = new Keyboard(this, R.xml.eng);
                break;
            case RUS:
                kb = new Keyboard(this, R.xml.rus);
                break;
            case UKR:
                kb = new Keyboard(this, R.xml.ukr);
                break;
        }
        if (caps || fakecaps) {
            caps = false;
            toCaps();
        }


    }

    private void toSym() {
        if (!sym)
            kb = new Keyboard(this, R.xml.sym);
        else
            rl();
        kv.setKeyboard(kb);
        sym = !sym;
    }

    private void toSym2() {
        if (!sym2)
            kb = new Keyboard(this, R.xml.sym2);
        else
            kb = new Keyboard(this, R.xml.sym);
        kv.setKeyboard(kb);
        sym2 = !sym2;
    }

    private void changeLang() {
        switch (cl) {
            case ENG:
                kb = new Keyboard(this, R.xml.rus);
                cl = LAYOUT.RUS;
                break;
            case RUS:
                kb = new Keyboard(this, R.xml.ukr);
                cl = LAYOUT.UKR;
                break;
            case UKR:
                kb = new Keyboard(this, R.xml.eng);
                cl = LAYOUT.ENG;
                break;
        }

        kv.setKeyboard(kb);
        if (fakecaps) {
            boolean wascaps=caps;
            caps = false;
            toCaps();
            caps=wascaps;
        }
        if(caps){
            caps=false;
            toCaps();
            caps=true;
        }
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                break;
            case Keyboard.KEYCODE_SHIFT:
                if (!caps && !fakecaps) {
                    toCaps();
                    fakecaps = true;
                    caps = false;
                } else if (fakecaps && !caps) {
                    caps = true;
                    fakecaps = false;
                } else if (caps) {
                    toCaps();
                }
                break;
            case Keyboard.KEYCODE_DONE:
                //https://stackoverflow.com/questions/30698657/android-handle-search-button-press-on-custom-keyboard
                final int options = this.getCurrentInputEditorInfo().imeOptions;
                final int actionId = options & EditorInfo.IME_MASK_ACTION;

                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        sendDefaultEditorAction(true);
                        break;
                    case EditorInfo.IME_ACTION_GO:
                        sendDefaultEditorAction(true);
                        break;
                    case EditorInfo.IME_ACTION_SEND:
                        sendDefaultEditorAction(true);
                        break;
                    default:
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                }
                break;
            case -2:
                toSym();
                break;
            case -8:
                changeLang();
                break;
            case 0:
                toSym2();
                break;
            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && (caps || fakecaps)) {
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code), 1);
                if (fakecaps) {
                    caps = true;
                    toCaps();
                    fakecaps = !fakecaps;
                }
        }
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        kb = new Keyboard(this, R.xml.eng);
        kv.setKeyboard(kb);
        kv.setOnKeyboardActionListener(this);

        //delete next string to turn on preview
        kv.setPreviewEnabled(false);

        cl = LAYOUT.ENG;
        toCaps();
        fakecaps = true;
        return kv;
    }


}