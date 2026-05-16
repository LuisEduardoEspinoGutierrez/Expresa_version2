package com.example.tt2.ejercicios;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlechaConexionView extends View {

    public interface OnConexionListener {
        void onConectado(String id);
        void onIncorrecto();
    }

    private OnConexionListener listener;
    private List<float[]> lineasConfirmadas = new ArrayList<>();
    private float lineaX1, lineaY1, lineaX2, lineaY2;
    private boolean dibujando = false;
    private RectF zonaR = new RectF();
    private String idActual = null;
    private Set<String> yaConectados = new HashSet<>();
    private Paint paintLinea, paintLineaTemp;

    public FlechaConexionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintLinea = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLinea.setColor(Color.parseColor("#4CAF50"));
        paintLinea.setStrokeWidth(8f);
        paintLinea.setStyle(Paint.Style.STROKE);

        paintLineaTemp = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLineaTemp.setColor(Color.parseColor("#2196F3"));
        paintLineaTemp.setStrokeWidth(8f);
        paintLineaTemp.setStyle(Paint.Style.STROKE);
    }

    public void setZonaR(RectF zona) {
        this.zonaR = zona;
    }

    public void setOnConexionListener(OnConexionListener l) {
        this.listener = l;
    }

    public void iniciarArrastre(String id, float x1, float y1) {
        if (yaConectados.contains(id)) return;
        idActual = id;
        lineaX1 = x1;
        lineaY1 = y1;
        lineaX2 = x1;
        lineaY2 = y1;
        dibujando = true;

        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        invalidate();
    }

    public void terminarArrastre(float x, float y) {
        if (!dibujando) return;
        dibujando = false;

        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(false);
        }

        if (zonaR.contains(x, y)) {
            if (listener != null) listener.onConectado(idActual);
        } else {
            if (listener != null) listener.onIncorrecto();
        }

        idActual = null;
        invalidate();
    }

    public void moverArrastre(float x, float y) {
        if (!dibujando) return;
        lineaX2 = x;
        lineaY2 = y;
        invalidate();
    }

    private void dibujarFlecha(Canvas canvas, float x1, float y1, float x2, float y2, Paint paint) {
        canvas.drawLine(x1, y1, x2, y2, paint);

        float dx = x2 - x1;
        float dy = y2 - y1;
        float angle = (float) Math.atan2(dy, dx);

        float arrowSize = 30f;

        float x3 = x2 - arrowSize * (float) Math.cos(angle - Math.PI / 6);
        float y3 = y2 - arrowSize * (float) Math.sin(angle - Math.PI / 6);

        float x4 = x2 - arrowSize * (float) Math.cos(angle + Math.PI / 6);
        float y4 = y2 - arrowSize * (float) Math.sin(angle + Math.PI / 6);

        canvas.drawLine(x2, y2, x3, y3, paint);
        canvas.drawLine(x2, y2, x4, y4, paint);
    }

    public void confirmarLinea() {
        lineasConfirmadas.add(new float[]{lineaX1, lineaY1, lineaX2, lineaY2});
        yaConectados.add(idActual);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Líneas confirmadas
        for (float[] l : lineasConfirmadas) {
            dibujarFlecha(canvas, l[0], l[1], l[2], l[3], paintLinea);
        }

        // Línea en movimiento
        if (dibujando) {
            dibujarFlecha(canvas, lineaX1, lineaY1, lineaX2, lineaY2, paintLineaTemp);
        }
    }

    public Set<String> getYaConectados() {
        return yaConectados;
    }

    /**
     * Restaura el estado de la vista desde una lista de líneas guardadas.
     */
    public void restaurarEstado(Set<String> conectados, List<float[]> lineas) {
        this.yaConectados = new HashSet<>(conectados);
        this.lineasConfirmadas = new ArrayList<>(lineas);
        invalidate();
    }

    public List<float[]> getLineasConfirmadas() {
        return lineasConfirmadas;
    }
}
