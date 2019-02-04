package com.example.inventoryappstage2.adapters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryappstage2.R;
import com.example.inventoryappstage2.activities.BookEditorActivity;
import com.example.inventoryappstage2.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = BookCursorAdapter.class.getSimpleName();

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView txtBookName = view.findViewById(R.id.tv_book_name);
        TextView txtBookAuthor = view.findViewById(R.id.tv_book_author);
        TextView txtBookPrice = view.findViewById(R.id.tv_book_price);
        TextView txtBookQuantity = view.findViewById(R.id.tv_book_quantity);

        //Set custom font to the text views
        txtBookName.setTypeface(Typeface.createFromAsset(context.getAssets(), "RubikMedium.ttf"));
        txtBookAuthor.setTypeface(Typeface.createFromAsset(context.getAssets(), "RubikRegular.ttf"));
        txtBookPrice.setTypeface(Typeface.createFromAsset(context.getAssets(), "RubikRegular.ttf"));
        txtBookQuantity.setTypeface(Typeface.createFromAsset(context.getAssets(), "RubikRegular.ttf"));

        //Find sale and edit buttons
        ImageView imgBtnSale = view.findViewById(R.id.img_btn_sale);
        ImageView imgBtnEdit = view.findViewById(R.id.img_btn_edit);

        //Find the columns of book attributes of the current book in focus
        int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
        int bookNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int bookAuthorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
        int bookPriceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int bookQuantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        //Read the book attributes from the Cursor for the current book
        String currentBookName = cursor.getString(bookNameColumnIndex);
        String currentBookAuthor = cursor.getString(bookAuthorColumnIndex);
        int currentBookPrice = cursor.getInt(bookPriceColumnIndex);
        final int currentBookQuantity = cursor.getInt(bookQuantityColumnIndex);

        txtBookName.setText(currentBookName);
        txtBookAuthor.setText(currentBookAuthor);
        if (currentBookPrice == 0) {
            txtBookPrice.setText(context.getString(R.string.unknown_price));
        } else {
            txtBookPrice.setText(String.format(context.getString(R.string.price_display), currentBookPrice));
        }
        // If the book quantity is empty string or null, then set it to zero.
        if (currentBookQuantity == 0) {
            txtBookQuantity.setText(context.getString(R.string.unknown_quantity));
            // Set colour of sale button and quantity text view to disabled
            imgBtnSale.setBackgroundResource(R.drawable.round_button_disabled);
            txtBookQuantity.setText(context.getString(R.string.out_of_stock));
            txtBookQuantity.setTextColor(Color.RED);
            // Disable button click
            imgBtnSale.setEnabled(false);
        } else {
            // Set colour of sale button and quantity text view to enabled
            imgBtnSale.setBackgroundResource(R.drawable.round_button_enabled);
            txtBookQuantity.setText(String.format(context.getString(R.string.in_stock), currentBookQuantity));
            txtBookQuantity.setTextColor(Color.BLUE);
            // Enable button click
            imgBtnSale.setEnabled(true);
        }

        //Adding listener to sale button
        //Clicking on it reduces the quantity by 1
        final String id = cursor.getString(idColumnIndex);

        imgBtnSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBookQuantity > 0) {
                    Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, Long.parseLong(id));
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, currentBookQuantity - 1);
                    context.getContentResolver().update(currentBookUri, values, null, null);
                    swapCursor(cursor);
                    if (currentBookQuantity == 1) {
                        Toast.makeText(context, R.string.toast_out_of_stock, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //Adding listener to the edit button
        //Opens item for editing
        imgBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BookEditorActivity.class);
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, Long.parseLong(id));
                intent.setData(currentBookUri);
                context.startActivity(intent);
            }
        });
    }
}
