package com.example.inventoryappstage2.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryappstage2.R;
import com.example.inventoryappstage2.data.BookContract.BookEntry;
import com.example.inventoryappstage2.utils.StringUtil;

import java.util.Objects;


public class BookDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = BookDetailsActivity.class.getSimpleName();

    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;

    /**
     * Content URI for the existing book
     */
    private Uri mCurrentBookUri;

    /**
     * TextView fields to enter the book details
     */
    private TextView txtBookName;
    private TextView txtBookAuthor;
    private TextView txtBookPrice;
    private TextView txtBookQuantity;
    private TextView txtSupplierName;
    private TextView txtSupplierPhoneNumber;

    /**
     * Int that holds the quantity for the + and - buttons
     */
    private int quantity;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        //Examine the intent that was used to launch this activity
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        Toolbar toolbar = findViewById(R.id.tb_book_details);
        toolbar.setTitle(R.string.toolbar_title_display_book_details);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txtBookName = findViewById(R.id.txt_book_name);
        txtBookAuthor = findViewById(R.id.txt_book_author);
        txtBookPrice = findViewById(R.id.txt_book_price);
        txtBookQuantity = findViewById(R.id.txt_book_quantity);
        txtSupplierName = findViewById(R.id.txt_supplier_name);
        txtSupplierPhoneNumber = findViewById(R.id.txt_supplier_number);

        //ImageButtons to increment/decrement quantity
        ImageButton imgIncrement = findViewById(R.id.img_btn_plus);
        imgIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(BookEntry.COLUMN_BOOK_QUANTITY, Integer.valueOf(txtBookQuantity.getText().toString()) + 1);
                getContentResolver().update(mCurrentBookUri, contentValues, null, null);
            }
        });
        ImageButton imgDecrement = findViewById(R.id.img_btn_minus);
        imgDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity = Integer.parseInt(txtBookQuantity.getText().toString().trim());
                if (quantity > 0) {
                    quantity--;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
                    getContentResolver().update(mCurrentBookUri, contentValues, null, null);
                }
            }
        });

        getSupportLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
        String[] projection = {BookEntry._ID, BookEntry.COLUMN_BOOK_NAME, BookEntry.COLUMN_BOOK_AUTHOR, BookEntry.COLUMN_BOOK_PRICE, BookEntry.COLUMN_BOOK_QUANTITY, BookEntry.COLUMN_SUPPLIER_NAME, BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, mCurrentBookUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Return early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int bookNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int bookAuthorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
            int bookPriceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int bookQuantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierNumberColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String bookName = cursor.getString(bookNameColumnIndex);
            String bookAuthor = cursor.getString(bookAuthorColumnIndex);
            int bookPrice = cursor.getInt(bookPriceColumnIndex);
            int bookQuantity = cursor.getInt(bookQuantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierNumber = cursor.getString(supplierNumberColumnIndex);

            // Update the views on the screen with the values from the database
            txtBookName.setText(bookName);
            txtBookAuthor.setText(bookAuthor);
            txtBookPrice.setText(Integer.toString(bookPrice));
            txtBookQuantity.setText(Integer.toString(bookQuantity));
            txtSupplierName.setText(supplierName);
            txtSupplierPhoneNumber.setText(supplierNumber);
            if (supplierNumber != null) {
                StringUtil.stripUnderlines((Spannable) txtSupplierPhoneNumber.getText());
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        txtBookName.setText("");
        txtBookAuthor.setText("");
        txtBookPrice.setText("");
        txtBookQuantity.setText("");
        txtSupplierName.setText("");
        txtSupplierPhoneNumber.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_book_details_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(BookDetailsActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful), Toast.LENGTH_SHORT).show();
            }
        }
        //Close the activity
        finish();
    }
}
