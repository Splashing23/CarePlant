package com.example.careplant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity  {

    private LineChart Temp_linechart;
    ArrayList<Entry> yData;
    DatabaseReference mPostReference;
    DatabaseReference mPostReference2;
    ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Temp_linechart = findViewById(R.id.chart1);

        Temp_linechart.getDescription().setEnabled(true);
        Temp_linechart.getDescription().setText("Realtime Moisture Levels");
        Temp_linechart.getDescription().setTextSize(10);
        Temp_linechart.getDescription().setTextColor(Color.rgb(5,5,46));

        Temp_linechart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        mPostReference = FirebaseDatabase.getInstance().getReference("/Data");
        mPostReference2 = FirebaseDatabase.getInstance().getReference("/WP");
        Button btn = findViewById(R.id.WP);
        btn.setOnClickListener(view -> mPostReference2.setValue(1));

        mPostReference.addValueEventListener(valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                int k = 0;
                float avgVal = 0;
                yData = new ArrayList<>();
                int j = (int) dataSnapshot.getChildrenCount();

                if (j > 100) {
                    k = j - 100;
                }

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    i = i + 1;
                    if (k >= i ) {
                        ds.getRef().removeValue();
                    }
                    else {
                        String SV = ds.child("ML").getValue().toString();
                        Long time = (Long) ds.child("time").getValue();

                        String time1 = String.valueOf(time - 1676600000000L);
                        float SensorValue = Float.parseFloat(SV);
                        avgVal = avgVal + SensorValue;
                        float tme = Float.parseFloat(time1);
                        yData.add(new Entry(tme, SensorValue));
                    }
                }

                final LineDataSet lineDataSet = new LineDataSet(yData,"Moisture Level");
                LineData data = new LineData(lineDataSet);
                avgVal = avgVal/yData.size();
                Temp_linechart.setData(data);
                Temp_linechart.notifyDataSetChanged();
                //LimitLine limitLine = new LimitLine(avgVal,"AVG Humidity");
                //limitLine.enableDashedLine(10,10,2);
                //Temp_linechart.getAxisLeft().addLimitLine(limitLine);


                XAxis xAxis = Temp_linechart.getXAxis();
                xAxis.setCenterAxisLabels(true);
                xAxis.setValueFormatter(new ValueFormatter() {

                    @Override
                    public String getFormattedValue(float value) {
                        Date dates = new Date((long)value);
                        Format formats = new SimpleDateFormat("HH:mm");
                        return formats.format(dates);
                    }
                });
                Temp_linechart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}