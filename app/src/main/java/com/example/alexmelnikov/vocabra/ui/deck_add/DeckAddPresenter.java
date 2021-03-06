package com.example.alexmelnikov.vocabra.ui.deck_add;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.example.alexmelnikov.vocabra.R;
import com.example.alexmelnikov.vocabra.VocabraApp;
import com.example.alexmelnikov.vocabra.data.DecksRepository;
import com.example.alexmelnikov.vocabra.data.LanguagesRepository;
import com.example.alexmelnikov.vocabra.data.UserDataRepository;
import com.example.alexmelnikov.vocabra.model.Deck;
import com.example.alexmelnikov.vocabra.model.Language;
import com.example.alexmelnikov.vocabra.model.SelectedLanguages;
import com.example.alexmelnikov.vocabra.ui.SnackBarActionHandler;
import com.example.alexmelnikov.vocabra.utils.LanguageUtils;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;

/**
 * DeckAddPresenter.java – presenter for DeckAddFragment
 * @author Alexander Melnikov
 */

@InjectViewState
public class DeckAddPresenter extends MvpPresenter<DeckAddView> implements SnackBarActionHandler {

    private static final String TAG = "MyTag";

    @Inject LanguagesRepository mLangRep;
    @Inject UserDataRepository mUserData;
    @Inject DecksRepository mDecksRep;

    private ArrayList<Language> mLangList;

    static int selectedColor;

    private int mSelectedFrom; //TranslatonFragment spinner index
    private int mSelectedTo; //TraxnslationFragment spinner index
    private String mSelectedToLanguage; //e.g. "Английский"
    private String mSelectedFromLanguage; //e.g. "Русский"

    private boolean nameEtErrorEnabled;

    DeckAddPresenter() {
        VocabraApp.getPresenterComponent().inject(this);
        mLangList = mLangRep.getLanguagesFromDB();
        Collections.sort(mLangList);

        SelectedLanguages selectedLanguages = (SelectedLanguages) mUserData.getValue(UserDataRepository.SELECTED_LANGUAGES, new SelectedLanguages(
                mLangList.indexOf(LanguageUtils.findByKey("ru")),
                mLangList.indexOf(LanguageUtils.findByKey("en"))));

        mSelectedFrom = selectedLanguages.from();
        mSelectedTo = selectedLanguages.to();

    }


    @Override
    public void attachView(DeckAddView view) {
        super.attachView(view);
        getViewState().attachInputListeners();
        getViewState().setupSpinners(mLangList, mSelectedFrom, mSelectedTo);
        getViewState().setupDefaultColor();
    }

    @Override
    public void detachView(DeckAddView view) {
        super.detachView(view);
        getViewState().detachInputListeners();
    }

    void selectorFrom(int index) {
        if (index == mSelectedTo) {
            swapSelection();
        } else {
            mSelectedFrom = index;
            updateSelectedLanguages();
        }
    }

    void selectorTo(int index) {
        if (index == mSelectedFrom) {
            swapSelection();
        } else {
            mSelectedTo = index;
            updateSelectedLanguages();
        }
    }

    void swapSelection() {
        int temp = mSelectedFrom;
        mSelectedFrom = mSelectedTo;
        mSelectedTo = temp;
        getViewState().changeLanguagesSelected(mSelectedFrom, mSelectedTo);

        updateSelectedLanguages();
    }

    void inputChanges(String text) {
        if (nameEtErrorEnabled) {
            nameEtErrorEnabled = false;
            getViewState().hideNameEditTextError();
        }
    }


    void colorChangeButtonPressed() {
        getViewState().showSelectColorDialog();
    }


    void updateSelectedColor(int color) {
        selectedColor = color;
        getViewState().updateCardColor(color);
    }


    void addNewDeckRequest(String name) {
        if (!name.trim().isEmpty()) {
            Deck deck = new Deck(-1, name.trim(), selectedColor,
                    mLangList.get(mSelectedFrom), mLangList.get(mSelectedTo));
            if (!mDecksRep.containsSimilarElementInDB(deck)) {
                mDecksRep.insertDeckToDB(deck);
                getViewState().closeFragment(mDecksRep.getDeckByName(deck.getName()).getId());
            } else {
                nameEtErrorEnabled = true;
                getViewState().showNameEditTextError("Колода с таким названием уже существует");
            }
        } else {
            nameEtErrorEnabled = true;
            getViewState().showNameEditTextError("Введите название");
        }
    }
    
    
    @Override
    public void onSnackbarEvent(int actionId) {}
    
    
    

    //==================Private logic=================

    private void updateSelectedLanguages() {
        try {
            mSelectedToLanguage = mLangList.get(mSelectedTo).getLang();
            mSelectedFromLanguage = mLangList.get(mSelectedFrom).getLang();
        } catch (NullPointerException e) {
            mSelectedToLanguage = mSelectedFromLanguage = "Error";
        }
        Log.d(TAG, "updateSelectedLanguages: " + mSelectedFromLanguage + "-" + mSelectedToLanguage);
    }

}
