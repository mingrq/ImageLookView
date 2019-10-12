package com.m.imagelookview;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.m.imagelookviewlib.ImageLookView;


public class MainActivity extends AppCompatActivity {
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageLookView imageView = findViewById(R.id.iv);
        Button button = findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setRotate(20);
            }
        });

     /*   Log.e("test1", imageView.getImageMatrix().toString());
        Log.e("test2", imageView.getDrawable().getBounds().toString());
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Rect rect = new Rect();
                imageView.getDrawingRect(rect);
                if (i == 2) {
                    Matrix matrix = imageView.getImageMatrix();
                    matrix.postTranslate(1, 1);
                    imageView.setImageMatrix(matrix);
                    imageView.invalidate();
                }
                i=2;
                Log.e("test", rect.toString());


                Log.e("test1", imageView.getImageMatrix().toString());
                Log.e("test2", imageView.getDrawable().getBounds().toString());
            }
        });
*/
    }
}
