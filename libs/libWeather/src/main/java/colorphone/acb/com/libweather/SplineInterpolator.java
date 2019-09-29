package colorphone.acb.com.libweather;

import java.util.List;

public class SplineInterpolator {

    private float[] x, y, h, d, m;
    private float[][] mat;
    private int n = 0, mSize = 0;
    private float[] p, r, g;
    private int mAllocSize = 0;

    public SplineInterpolator(int size) {
        this.mSize = size;
        allocArrays(mSize);
    }

    private void allocArrays(int size) {
        if (size <= mAllocSize) {
            return;
        }
        x = new float[size];
        y = new float[size];
        h = new float[size];
        d = new float[size];
        m = new float[size];
        mat = new float[size][size];

        p = new float[size];
        r = new float[size];
        g = new float[size];
        mAllocSize = size;
    }

    public void load(List<Float> coordX, List<Float> coordY) {
        mSize = Math.min(mSize, Math.min(coordX.size(), coordY.size()));
        allocArrays(mSize);
        n = mSize - 1;
        for (int i = 0; i < mSize; ++i) {
            x[i] = coordX.get(i);
            y[i] = coordY.get(i);
        }
        buildEquations();
        solveEquations();
    }

    public float interpolate(float coordX) {
        for (int i = 0; i < n; ++i) {
            if (coordX <= x[i + 1]) {
                float A = x[i + 1] - coordX;
                float B = coordX - x[i];
                return m[i] * A * A * A / (6.0f * h[i]) + m[i + 1] * B * B * B / (6.0f * h[i]) + (y[i] - m[i] * h[i] * h[i] / 6.0f) * A / h[i] + (y[i + 1] - m[i + 1] * h[i] * h[i] / 6.0f) * B / h[i];
            }
        }
        return 0;
    }

    private void buildEquations() {
        for (int i = 0; i < n; ++i)
            h[i] = x[i + 1] - x[i];
        mat[0][0] = 2f;
        mat[0][1] = 1f;
        float f0 = 0;
        d[0] = 6.0f * ((y[1] - y[0]) / h[0] - f0) / h[0];
        mat[n][n - 1] = 1f;
        mat[n][n] = 2f;
        float fn = 0;
        d[n] = 6.0f * (fn - (y[n] - y[n - 1]) / h[n - 1]) / h[n - 1];
        for (int i = 1; i < n; ++i) {
            mat[i][i] = 2f;
            mat[i][i - 1] = h[i - 1] / (h[i - 1] + h[i]);
            mat[i][i + 1] = 1 - mat[i][i - 1];
            d[i] = 6.0f * ((y[i + 1] - y[i]) / h[i] - (y[i] - y[i - 1]) / h[i - 1]) / (h[i] + h[i - 1]);
        }
    }

    private void solveEquations() {
        p[0] = mat[0][0];
        g[0] = d[0] / p[0];
        for (int i = 1; i <= n; ++i) {
            r[i - 1] = mat[i - 1][i] / p[i - 1];
            p[i] = mat[i][i] - mat[i][i - 1] * r[i - 1];
            g[i] = (d[i] - mat[i][i - 1] * g[i - 1]) / p[i];
        }
        m[n] = g[n];
        for (int i = n - 1; i >= 0; --i) {
            m[i] = g[i] - r[i] * m[i + 1];
        }
    }
}
