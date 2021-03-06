package com.example.alexmelnikov.vocabra.ui.cardbrowser;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.transition.ChangeBounds;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.PresenterType;
import com.example.alexmelnikov.vocabra.R;
import com.example.alexmelnikov.vocabra.adapter.CardsAdapter;
import com.example.alexmelnikov.vocabra.adapter.DecksDialogAdapter;
import com.example.alexmelnikov.vocabra.adapter.DecksSpinnerAdapter;
import com.example.alexmelnikov.vocabra.adapter.SortMethodsDialogAdapter;
import com.example.alexmelnikov.vocabra.adapter.layout_manager.CardsLinearLayoutManager;
import com.example.alexmelnikov.vocabra.model.Card;
import com.example.alexmelnikov.vocabra.model.CardSortMethod;
import com.example.alexmelnikov.vocabra.model.Deck;
import com.example.alexmelnikov.vocabra.model.Language;
import com.example.alexmelnikov.vocabra.ui.BaseFragment;
import com.example.alexmelnikov.vocabra.ui.deck_add.DeckAddFragment;
import com.example.alexmelnikov.vocabra.ui.main.MainActivity;
import com.example.alexmelnikov.vocabra.utils.LanguageUtils;
import com.jakewharton.rxbinding2.view.RxView;
import com.thebluealliance.spectrum.SpectrumDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;


/**
 * Created by AlexMelnikov on 24.02.18.
 */


public class CardBrowserFragment extends BaseFragment implements CardBrowserView {

    private static final String TAG = "MyTag";

    @InjectPresenter(type = PresenterType.GLOBAL, tag = "cardbrowser")
    CardBrowserPresenter mCardBrowserPresenter;

    @BindView(R.id.layout_toolbar) RelativeLayout rlToolbar;

    @BindView(R.id.btn_decks) ImageButton btnDecks;
    @BindView(R.id.btn_back) ImageButton btnBack;
    @BindView(R.id.btn_sort) ImageButton btnSort;
    @BindView(R.id.rv_cards) RecyclerView rvCards;
    @BindView(R.id.fab_add) FloatingActionButton btnAddCard;
    @BindView(R.id.layout_decks_btn) LinearLayout layoutDecksBtn;

    @BindView(R.id.layout_deck_cards) LinearLayout layoutDeckCards;
    @BindView(R.id.rl_deck) RelativeLayout rlDeck;
    @BindView(R.id.tv_deck_name) TextView tvDeckName;
    @BindView(R.id.tv_deck_langs) TextView tvDeckLangs;
    @BindView(R.id.btn_edit_deck) ImageButton btnEditDeck;
    @BindView(R.id.btn_delete_deck) ImageButton btnDeleteDeck;
    @BindView(R.id.btn_reset_deck) ImageButton btnResetDeck;
    @BindView(R.id.btn_edit_color) ImageButton btnEditColor;
    @BindView(R.id.btn_confirm) ImageButton btnConfirm;

    @BindView(R.id.et_deck_name) EditText etDeckName;

    @BindView(R.id.tv_new_counter) TextView tvNewCounter;
    @BindView(R.id.tv_ready_counter) TextView tvReadyCouner;

    @BindView(R.id.layout_toolbar_edit_mode) RelativeLayout rlToolbarEditMode;
    @BindView(R.id.btn_delete_cards) ImageButton btnDeleteItems;
    @BindView(R.id.btn_reset_cards) ImageButton btnResetCards;
    @BindView(R.id.cb_select_all) CheckBox cbSelectAll;
    @BindView(R.id.tv_selected_counter) TextView tvSelectedCounter;

    //addCardDialog/editCardDialog views
    EditText etDialogFront;
    EditText etDialogBack;
    EditText etDialogContext;
    Spinner mDialogSpinDecks;
    TextInputLayout mDialogTilFront;
    TextInputLayout mDialogTilBack;
    TextInputLayout mDialogTilContext;


    private CardsAdapter mCardsAdapter;
    private LinearLayoutManager mCardsRvManager;

    private MaterialDialog decksDialog;
    //RecyclerView used in decks dialog
    private RecyclerView rvDecks;

    private MaterialDialog sortMethodsDialog;
    //RecyclerView used in sort methods dialog
    private RecyclerView rvSortMethods;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_browser, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCardsAdapter = new CardsAdapter(getActivity(), new ArrayList<Card>(), mCardBrowserPresenter);
        mCardsAdapter.setHasStableIds(true);

        mCardBrowserPresenter.initSortingMethods(new String[]{getActivity().getResources().getString(R.string.card_sort_method_1),
                getActivity().getResources().getString(R.string.card_sort_method_2), getActivity().getResources().getString(R.string.card_sort_method_3)});

        etDeckName.setInputType(InputType.TYPE_CLASS_TEXT);

        mCardsRvManager = new CardsLinearLayoutManager(getActivity());
        rvCards.setLayoutManager(mCardsRvManager);
        rvCards.setAdapter(mCardsAdapter);

        /* If fragment called on popbackstack from DeckAddFragment, get createdDeckId from Activity
        *  and show it on screen using decksDialogRecyclerItemPressed method */
        int createdDeckId = ((MainActivity)getActivity()).getDeckId();
        if (createdDeckId != -1) {
            mCardBrowserPresenter.decksDialogRecyclerItemPressed(createdDeckId);
            ((MainActivity)getActivity()).resetCreatedDeckId();
        }

    }

    @Override
    public void attachInputListeners() {
        Disposable addCardButton = RxView.clicks(btnAddCard)
                .subscribe(o -> mCardBrowserPresenter.addButtonPressed());

        Disposable decksButton = RxView.clicks(btnDecks)
                .subscribe(o -> mCardBrowserPresenter.decksButtonPressed());

        Disposable decksLayout = RxView.clicks(layoutDecksBtn)
                .subscribe(o -> mCardBrowserPresenter.decksButtonPressed());

        Disposable backButton = RxView.clicks(btnBack)
                .subscribe(o -> mCardBrowserPresenter.backButtonPressed());

        Disposable deckNameText = RxView.clicks(tvDeckName)
                .subscribe(o -> mCardBrowserPresenter.editDeckButtonPressed());

        Disposable editDeckButton = RxView.clicks(btnEditDeck)
                .subscribe(o -> mCardBrowserPresenter.editDeckButtonPressed());

        Disposable confirmDeckEdit = RxView.clicks(btnConfirm)
                .subscribe(o -> mCardBrowserPresenter.confirmEditDeckRequest(etDeckName.getText().toString()));

        Disposable editColor = RxView.clicks(btnEditColor)
                .subscribe(o -> mCardBrowserPresenter.editDeckColorRequest());

        Disposable sortButton = RxView.clicks(btnSort)
                .subscribe(o -> mCardBrowserPresenter.sortButtonPressed());

        Disposable deleteDeckButton = RxView.clicks(btnDeleteDeck)
                .subscribe(o -> mCardBrowserPresenter.deleteDeckButtonPressed());

        Disposable resetDeckButton = RxView.clicks(btnResetDeck)
                .subscribe(o -> mCardBrowserPresenter.resetDeckButtonPressed());

        Disposable deleteCardsButton = RxView.clicks(btnDeleteItems)
                .subscribe(o -> mCardBrowserPresenter.deleteItemsRequest(mCardsAdapter.getSelectedItemsIndexes()));

        Disposable resetCardsButton = RxView.clicks(btnResetCards)
                .subscribe(o -> mCardBrowserPresenter.resetItemsStatsButtonPressed(mCardsAdapter.getSelectedItemsIndexes()));



        cbSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    mCardsAdapter.selectAllItems();
                else
                    mCardsAdapter.unselectAllItems();
            }
        });

        mDisposable.addAll(addCardButton, decksButton, editDeckButton, confirmDeckEdit, backButton,
                editColor, deckNameText, decksLayout, sortButton, deleteCardsButton, deleteDeckButton,
                resetCardsButton, resetDeckButton);
    }


    @Override
    public void detachInputListeners() {
        mDisposable.clear();
    }

    @Override
    public void replaceCardsRecyclerData(ArrayList<Card> cards) {
        mCardsAdapter.replaceData(cards);
    }

    @Override
    public void setupCounters(int newCounter, int oldReadyCounter) {
        tvNewCounter.setText(newCounter + "");
        tvReadyCouner.setText(oldReadyCounter + "");
    }

    @Override
    public void showDecksListDialog(ArrayList<Deck> decks) {
        decksDialog = new MaterialDialog.Builder(getActivity())
                .title("Мои колоды")
                .customView(R.layout.dialog_decks, false)
                .positiveText("Добавить колоду")
                .negativeText("Назад")
                .onPositive(((dialog1, which) -> mCardBrowserPresenter.createNewDeckRequest()))
                .build();

        Log.d(TAG, "showDecksListDialog: " + decks.size());
        rvDecks = decksDialog.getView().findViewById(R.id.rv_decks);
        rvDecks.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvDecks.setAdapter(new DecksDialogAdapter(getActivity(), decks, mCardBrowserPresenter));

        decksDialog.show();
    }

    @Override
    public void hideDecksListDialog() {
        try {
            decksDialog.hide();
        } catch (NullPointerException e) {
            Log.e(TAG, "hideDecksListDialog: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void showSortOptionsDialog(ArrayList<CardSortMethod> methods,
                                      CardSortMethod currentMethod, int currentMethodIndex) {
        sortMethodsDialog = new MaterialDialog.Builder(getActivity())
                .title("Сортировка")
                .customView(R.layout.dialog_sort_methods, false)
                //.onPositive(((dialog1, which) -> mCardBrowserPresenter.createNewDeckRequest()))
                .build();

        rvSortMethods = sortMethodsDialog.getView().findViewById(R.id.rv_methods);
        rvSortMethods.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvSortMethods.setAdapter(new SortMethodsDialogAdapter(getActivity(), methods,
                currentMethod, currentMethodIndex, mCardBrowserPresenter));

        sortMethodsDialog.show();
    }

    @Override
    public void hideSortOptionstDialog() {
        sortMethodsDialog.hide();
    }


    @Override
    public void openDeckCreationFragment() {
        DeckAddFragment fragment = new DeckAddFragment();

        ((MainActivity)getActivity()).hideBottomNavigationBar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ChangeBounds changeBoundsTransition = new ChangeBounds();
            changeBoundsTransition.setDuration(370);

            setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));

            fragment.setEnterTransition(new Slide().setDuration(370));
            fragment.setSharedElementReturnTransition(changeBoundsTransition);
        }


        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .addSharedElement(btnAddCard, "fabAdd")
                .commitAllowingStateLoss();
    }

    @Override
    public void showDeckCardview(Deck deck) {
        tvDeckName.setText(deck.getName());
        tvDeckLangs.setText(deck.getFirstLanguage().getLang() + "-" + deck.getSecondLanguage().getLang());
        final Drawable drawable = getActivity().getResources().getDrawable(R.drawable.bg_card);
        drawable.setColorFilter(deck.getColor(), PorterDuff.Mode.SRC_ATOP);
        rlDeck.setBackground(drawable);
        rlDeck.setVisibility(View.VISIBLE);
    }

    @Override
    public void switchResetDeckButton(boolean enabled) {
        if (enabled)
            btnResetDeck.setEnabled(false);
        else
            btnResetDeck.setEnabled(true);
    }

    @Override
    public void switchDeckDisplayMode(boolean editModeOn) {
        if (editModeOn) {
            String deckName = tvDeckName.getText().toString();
            etDeckName.setVisibility(View.VISIBLE);
            tvDeckName.setVisibility(View.INVISIBLE);
            tvDeckLangs.setVisibility(View.INVISIBLE);
            etDeckName.setText(deckName);

            etDeckName.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etDeckName, 0);
            etDeckName.setSelection(etDeckName.getText().length());

            btnEditDeck.setVisibility(View.GONE);
            btnDeleteDeck.setVisibility(View.GONE);
            btnResetDeck.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.VISIBLE);
            btnEditColor.setVisibility(View.VISIBLE);
        } else {
            if (etDeckName.getVisibility() == View.VISIBLE) {
                String deckName = etDeckName.getText().toString();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                etDeckName.setVisibility(View.INVISIBLE);
                tvDeckName.setText(deckName);
                tvDeckName.setVisibility(View.VISIBLE);
                tvDeckLangs.setVisibility(View.VISIBLE);
                btnEditDeck.setVisibility(View.VISIBLE);
                btnDeleteDeck.setVisibility(View.VISIBLE);
                btnResetDeck.setVisibility(View.VISIBLE);
                btnConfirm.setVisibility(View.GONE);
                btnEditColor.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void showAddCardDialog(ArrayList<Deck> decks, @Nullable Deck currentDeck) {
        MaterialDialog dialog =
                new MaterialDialog.Builder(getActivity())
                        .title("Добавление карточки")
                        .customView(R.layout.dialog_add_card, true)
                        .positiveText("Добавить")
                        .negativeText(android.R.string.cancel)
                        .autoDismiss(false)
                        .onNegative(((dialog1, which) -> dialog1.dismiss()))
                        .onPositive((dialog1, which) -> {
                            if (etDialogFront.getText().toString().trim().isEmpty())
                                mDialogTilFront.setError("Введите слово или фразу");
                            if (etDialogBack.getText().toString().trim().isEmpty())
                                mDialogTilBack.setError("Введите слово или фразу");
                            if (!etDialogFront.getText().toString().trim().isEmpty() &&
                                    !etDialogBack.getText().toString().trim().isEmpty()) {
                                mCardBrowserPresenter.addNewCardRequest(etDialogFront.getText().toString(),
                                        etDialogBack.getText().toString(),
                                        LanguageUtils.findByKey(mDialogTilFront.getHint().toString().split(":")[1].trim()),
                                        LanguageUtils.findByKey(mDialogTilBack.getHint().toString().split(":")[1].trim()),
                                        etDialogContext.getText().toString(),
                                        (String) mDialogSpinDecks.getSelectedItem(),
                                        getResources().getColor(R.color.colorPrimary));
                                dialog1.dismiss();
                            }
                        })
                        .build();

        etDialogFront = (EditText) dialog.getView().findViewById(R.id.et_front);
        etDialogBack = (EditText) dialog.getView().findViewById(R.id.et_back);
        etDialogContext = (EditText) dialog.getView().findViewById(R.id.et_context);
        mDialogSpinDecks = (Spinner) dialog.getView().findViewById(R.id.spin_decks);
        mDialogTilFront = (TextInputLayout) dialog.getView().findViewById(R.id.input_layout_front);
        mDialogTilBack = (TextInputLayout) dialog.getView().findViewById(R.id.input_layout_back);
        mDialogTilContext = (TextInputLayout) dialog.getView().findViewById(R.id.input_layout_context);

        mDialogSpinDecks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                List<Language> langs = mCardBrowserPresenter.getLanguagesByDeckName(((TextView)view.findViewById(R.id.line_one)).getText().toString());
                mDialogTilFront.setHint("Передняя сторона : " + langs.get(0).getId());
                mDialogTilBack.setHint("Задняя сторона : " + langs.get(1).getId());

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        etDialogFront.setInputType(InputType.TYPE_CLASS_TEXT);
        etDialogBack.setInputType(InputType.TYPE_CLASS_TEXT);
        etDialogContext.setInputType(InputType.TYPE_CLASS_TEXT);
        mDialogTilFront.setHint("Передняя сторона");
        mDialogTilBack.setHint("Задняя сторона");
        mDialogTilContext.setError("Контекст поможет новому слову лучше отложиться в памяти");
        etDialogFront.requestFocus();
        mDialogSpinDecks.setAdapter(new DecksSpinnerAdapter(getActivity(), decks, false));

        //Display only chosen deck if dialog called on current deck cards
        if (currentDeck != null) {
            int index = 0;
            for (int i = 0; i < mDialogSpinDecks.getCount(); i++) {
                if (mDialogSpinDecks.getItemAtPosition(i).toString().equals(currentDeck.getName())) {
                    index = i;
                    break;
                }
            }
            mDialogSpinDecks.setSelection(index);
            mDialogSpinDecks.setEnabled(false);
        }

        dialog.show();
    }


    @Override
    public void showEditCardDialog(Card card, ArrayList<Deck> decks) {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title("Изменение карточки")
                        .customView(R.layout.dialog_add_card, true)
                        .positiveText("Сохранить")
                        .negativeText(android.R.string.cancel)
                        .autoDismiss(false)
                        .onNegative(((dialog1, which) -> {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                            dialog1.dismiss();
                        }))
                        .onPositive((dialog1, which) -> {
                            if (etDialogFront.getText().toString().trim().isEmpty())
                                mDialogTilFront.setError("Введите слово или фразу");
                            if (etDialogBack.getText().toString().trim().isEmpty())
                                mDialogTilBack.setError("Введите слово или фразу");

                            if (!etDialogFront.getText().toString().trim().isEmpty() && !etDialogBack.getText().toString().trim().isEmpty()) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                                mCardBrowserPresenter.editCardRequest(card, etDialogFront.getText().toString(), etDialogBack.getText().toString(),
                                        etDialogContext.getText().toString(), mDialogSpinDecks.getSelectedItem().toString());
                                dialog1.dismiss();
                            }
                        })
                        .build();

        etDialogFront = (EditText) dialog.getView().findViewById(R.id.et_front);
        etDialogBack = (EditText) dialog.getView().findViewById(R.id.et_back);
        etDialogContext = (EditText) dialog.getView().findViewById(R.id.et_context);
        mDialogSpinDecks = (Spinner) dialog.getView().findViewById(R.id.spin_decks);
        mDialogTilFront = (TextInputLayout) dialog.getView().findViewById(R.id.input_layout_front);
        mDialogTilBack = (TextInputLayout) dialog.getView().findViewById(R.id.input_layout_back);
        mDialogTilContext = (TextInputLayout) dialog.getView().findViewById(R.id.input_layout_context);

        mDialogSpinDecks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                List<Language> langs = mCardBrowserPresenter.getLanguagesByDeckName(((TextView)view.findViewById(R.id.line_one)).getText().toString());
                mDialogTilFront.setHint("Передняя сторона : " + langs.get(0).getId());
                mDialogTilBack.setHint("Задняя сторона : " + langs.get(1).getId());

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        etDialogFront.setInputType(InputType.TYPE_CLASS_TEXT);
        etDialogBack.setInputType(InputType.TYPE_CLASS_TEXT);
        etDialogContext.setInputType(InputType.TYPE_CLASS_TEXT);
        etDialogFront.setText(card.getFront());
        etDialogBack.setText(card.getBack());
        if (!card.getCardContext().isEmpty())
            etDialogContext.setText(card.getCardContext());
        mDialogTilFront.setHint("Передняя сторона");
        mDialogTilBack.setHint("Задняя сторона");
        if (card.getCardContext().isEmpty())
            mDialogTilContext.setError("Контекст поможет новому слову лучше отложиться в памяти");
        etDialogFront.requestFocus();

        etDialogFront.setSelection(etDialogFront.getText().length());
        etDialogBack.setSelection(etDialogBack.getText().length());
        mDialogSpinDecks.setAdapter(new DecksSpinnerAdapter(getActivity(), decks, false));

        int index = 0;
        for (int i = 0; i < mDialogSpinDecks.getCount(); i++) {
            if (mDialogSpinDecks.getItemAtPosition(i).toString().equals(card.getDeck().getName())) {
                index = i;
                break;
            }
        }
        mDialogSpinDecks.setSelection(index);

        dialog.show();
    }


    @Override
    public void hideDeckCardview() {
        rlDeck.setVisibility(View.GONE);
    }

    @Override
    public void switchCornerButtonState(boolean showingDeckCards) {
        if (showingDeckCards) {
            layoutDecksBtn.setVisibility(View.GONE);
            btnBack.setVisibility(View.VISIBLE);
        }
        else {
            btnBack.setVisibility(View.GONE);
            layoutDecksBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showSelectColorDialog(Deck currentDeck) {
        TypedArray colors = getResources().obtainTypedArray(R.array.custom_colors);
        int[] colorsInts = new int[colors.length()];
        for (int i = 0; i < colors.length(); i++) {
            colorsInts[i] = colors.getColor(i,0);
        }

        new SpectrumDialog.Builder((getContext()))
                .setTitle("Цвет колоды")
                .setColors(R.array.custom_colors)
                .setDismissOnColorSelected(true)
                .setSelectedColor(mCardBrowserPresenter.selectedColor)
                .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(boolean positiveResult, int color) {
                        mCardBrowserPresenter.editDeckColorResultPassed(color);
                    }
                }).build().show(getFragmentManager(), "color_dialog");
    }

    public void showDeleteDeckConfirmationDialog(Deck deck) {
            new MaterialDialog.Builder(getActivity())
                    .content("Вы уверены, что хотите удалить колоду " + deck.getName() + " и все ее карточки?")
                    .positiveText("Подтвердить")
                    .negativeText("Отмена")
                    .onPositive((dialog, which) -> mCardBrowserPresenter.deleteDeckRequestConfirmed())
                    .show();
    }

    public void showResetDeckConfirmationDialog(Deck deck) {
        new MaterialDialog.Builder(getActivity())
                .content("Сбросить статистику карточек колоды " + deck.getName() + " ?")
                .positiveText("Подтвердить")
                .negativeText("Отмена")
                .onPositive((dialog, which) -> mCardBrowserPresenter.resetDeckRequestConfirmed())
                .show();
    }

    public void showResetStatsItemsConfirmationDialog(boolean[] selectedItemsIndexes) {
        new MaterialDialog.Builder(getActivity())
                .content("Сбросить статистику повторений выбранных карточек?")
                .positiveText("Подтвердить")
                .negativeText("Отмена")
                .onPositive((dialog, which) -> mCardBrowserPresenter.resetItemsStatsRequestConfirmed(selectedItemsIndexes))
                .show();
    }

    @Override
    public void updateCardColor(int color) {
        final Drawable drawable = getActivity().getResources().getDrawable(R.drawable.bg_card);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        rlDeck.setBackground(drawable);
    }

    @Override
    public void enableEditModeToolbar(int firstSelectedItemIndex) {
        rlToolbarEditMode.setVisibility(View.VISIBLE);
        mCardsAdapter.enableSelectMode(firstSelectedItemIndex);
        rlToolbar.setVisibility(View.GONE);
        btnAddCard.animate().yBy(300).setDuration(350)
                .withEndAction(
                new Runnable() {
                    @Override
                    public void run() {
                        btnAddCard.setVisibility(View.GONE);
                    }
                }
        );
        btnAddCard.setVisibility(View.GONE);
    }

    @Override
    public void disableEditModeToolbar() {
        mCardsAdapter.unselectAllItems();
        mCardsAdapter.disableSelectMode();
        cbSelectAll.setChecked(false);
        rlToolbar.setVisibility(View.VISIBLE);
        btnAddCard.setVisibility(View.VISIBLE);
        btnAddCard.animate().yBy(-300).setDuration(350);
        rlToolbarEditMode.setVisibility(View.GONE);
    }

    @Override
    public void updateSelectedCounter(int count, int max) {
            tvSelectedCounter.setText(Integer.toString(count));
            if (count == max && !cbSelectAll.isChecked()) {
                cbSelectAll.setChecked(true);
            } else if (count == 0 && cbSelectAll.isChecked()) {
                cbSelectAll.setChecked(false);
            }
    }

    @Override
    public void changeAddButtonResource(boolean addDeckMode) {
        if (addDeckMode)
            btnAddCard.setImageResource(R.drawable.ic_library_add_white_30dp);
        else
            btnAddCard.setImageResource(R.drawable.ic_add_white_30dp);

    }

    @Override
    public void showDeckNameEditTextMessage(String message) {
        etDeckName.setError(message);
    }

    @Override
    public void showCardAlreadyExistsSnackbarMessageAction(String deckName) {
        ((MainActivity)getActivity()).showMessage(1, "Такая карточка уже существует в колоде " + deckName,
                true, mCardBrowserPresenter, "Еще раз");
    }

    @Override
    public void showCardAlreadyExistsSnackbarMessage(String deckName) {
        ((MainActivity)getActivity()).showMessage(0, "Такая карточка уже существует в колоде " + deckName,
                false, null, null);
    }

    @Override
    public void showCardSuccessfulyAddedSnackbarMessage(String deckName) {
        ((MainActivity)getActivity()).showMessage(0, "Карточка успешно добавлена в " + deckName, false, null, null);
    }

    @Override
    public void showSelectedItemsDeletedMessage() {
        ((MainActivity)getActivity()).showMessage(2, "Выделенные элементы были удалены",
                true, mCardBrowserPresenter, "Отменить");
    }

    @Override
    public void showSelectedItemsStatsResetedMessage() {
        ((MainActivity)getActivity()).showMessage(0, "Статистика повторений выделенных элементов была сброшена",
                false, null, null);
    }

    @Override
    public void showDeckDeletedMessage(String deckName) {
        ((MainActivity)getActivity()).showMessage(0, "Колода " + deckName + " была удалена",
                false, null, null);
    }

    @Override
    public void showDeckResetedMessage(String deckName) {
        ((MainActivity)getActivity()).showMessage(0, "Статистика повторений карточек колоды " + deckName + " была сброшена",
                false, null, null);
    }

    @Override
    public boolean onBackPressed() {
        if (etDeckName.getVisibility() == View.VISIBLE ||
                rlDeck.getVisibility() == View.VISIBLE ||
                rlToolbarEditMode.getVisibility() == View.VISIBLE) {
            mCardBrowserPresenter.backButtonPressed();
            return true;
        } else {
            return super.onBackPressed();
        }
    }
}
