package com.example.alexmelnikov.vocabra.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alexmelnikov.vocabra.R;
import com.example.alexmelnikov.vocabra.model.Card;
import com.example.alexmelnikov.vocabra.ui.cardbrowser.CardBrowserPresenter;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AlexMelnikov on 10.03.18.
 */

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardsViewHolder> {

    private static final String TAG = "MyTag";

    Context mContext;
    ArrayList<Card> mData;
    CardBrowserPresenter presenter;

    boolean selectMode;
    boolean[] selectedItems;
    int selectedItemsCounter;


    public CardsAdapter(Context mContext, ArrayList<Card> mData, CardBrowserPresenter presenter) {
        this.mContext = mContext;
        this.mData = mData;
        this.presenter = presenter;
        this.selectMode = false;
    }

    class CardsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_color)
        ImageView ivColor;
        @BindView(R.id.tv_front)
        TextView tvFront;
        @BindView(R.id.tv_back)
        TextView tvBack;
        @BindView(R.id.cb_select)
        CheckBox cbSelect;

        public CardsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public CardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CardsViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.item_card_list, parent, false));
    }

    @Override
    public void onBindViewHolder(CardsViewHolder holder, int position) {
        Card card = mData.get(mData.size() - position - 1);


        Log.d(TAG, "onBindViewHolder: " + selectedItemsCounter);
        holder.tvFront.setText(card.getFront());
        holder.tvBack.setText(card.getBack());

        final Drawable drawable = mContext.getResources().getDrawable(R.drawable.bg_color);
        drawable.setColorFilter(card.getDeck().getColor(), PorterDuff.Mode.SRC_ATOP);
        holder.ivColor.setBackground(drawable);

        if (selectMode) {
            holder.cbSelect.setVisibility(View.VISIBLE);

            if (selectedItems[mData.size() - position - 1])
                holder.cbSelect.setChecked(true);
            else
                holder.cbSelect.setChecked(false);


            holder.cbSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.cbSelect.isChecked()) {
                        selectedItems[mData.size() - position - 1] = true;
                        selectedItemsCounter++;
                    } else {
                        selectedItems[mData.size() - position - 1] = false;
                        selectedItemsCounter--;
                    }
                    presenter.updateSelectedItemsCount(selectedItemsCounter);
                }
            });

        } else {
            holder.cbSelect.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            if (!selectMode)
                presenter.cardsRecyclerItemPressed(mData.size() - position - 1);
            else {
                if (!holder.cbSelect.isChecked()) {
                    holder.cbSelect.setChecked(true);
                    selectedItems[mData.size() - position - 1] = true;
                    selectedItemsCounter++;
                } else {
                    holder.cbSelect.setChecked(false);
                    selectedItems[mData.size() - position - 1] = false;
                    selectedItemsCounter--;
                }
                presenter.updateSelectedItemsCount(selectedItemsCounter);
            }
        });


        holder.itemView.setOnLongClickListener(view -> {
            if (!selectMode)
                presenter.cardsRecyclerItemLongPressed(mData.size() - position - 1);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return mData.get(mData.size() - position - 1).hashCode();
    }

    public void replaceData(ArrayList<Card> cards) {
        mData.clear();
        mData.addAll(cards);
        selectedItems = new boolean[mData.size()];
        Arrays.fill(selectedItems, false);
        notifyDataSetChanged();
    }

    public void enableSelectMode(int firstSelectedIndex) {
        selectMode = true;
        selectedItems[firstSelectedIndex] = true;
        selectedItemsCounter = 1;
        Log.d(TAG, "enableSelectMode: " + selectedItemsCounter);
        presenter.updateSelectedItemsCount(selectedItemsCounter);
        notifyDataSetChanged();
    }

    public void disableSelectMode() {
        selectMode = false;
        selectedItemsCounter = 0;
        Log.d(TAG, "disableSelectMode: " + selectedItemsCounter);
        notifyDataSetChanged();
    }

    public void selectAllItems() {
        Arrays.fill(selectedItems, true);
        selectedItemsCounter = selectedItems.length;
        presenter.updateSelectedItemsCount(selectedItemsCounter);
        notifyDataSetChanged();
    }

    public void unselectAllItems() {
        Arrays.fill(selectedItems, false);
        selectedItemsCounter = 0;
        presenter.updateSelectedItemsCount(selectedItemsCounter);
        notifyDataSetChanged();
    }

    public boolean[] getSelectedItemsIndexes() {
        return selectedItems;
    }
}
