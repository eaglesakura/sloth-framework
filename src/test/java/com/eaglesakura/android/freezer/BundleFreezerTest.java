package com.eaglesakura.android.freezer;

import com.eaglesakura.android.framework.UnitTestCase;
import com.eaglesakura.freezer.BundleFreezer;
import com.eaglesakura.freezer.BundleState;
import com.eaglesakura.util.RandomUtil;

import org.junit.Test;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BundleFreezerTest extends UnitTestCase {

    @Test
    public void オブジェクトの保存と復旧が行える() throws Throwable {

        for (int i = 0; i < 10000; ++i) {
            Bundle state = new Bundle();
            SampleObject saveTarget = new SampleObject();
            SampleObject restoreTarget = new SampleObject();

            assertNotEquals(saveTarget, restoreTarget);

            BundleFreezer.create(saveTarget)
                    .state(state)
                    .save();

            BundleFreezer.create(restoreTarget)
                    .state(state)
                    .restore();

            assertEquals(saveTarget, restoreTarget);
        }
    }


    static class SampleObject {
        @BundleState
        boolean mBoolean = RandomUtil.randBool();
        @BundleState
        byte mByte = RandomUtil.randInt8();
        @BundleState
        short mShort = RandomUtil.randInt16();
        @BundleState
        int mInt = RandomUtil.randInt32();
        @BundleState
        long mLong = RandomUtil.randInt64();
        @BundleState
        float mFloat = RandomUtil.randFloat();
        @BundleState
        double mDouble = RandomUtil.randFloat();
        @BundleState
        String mString = RandomUtil.randShortString();

        @BundleState
        boolean[] mBooleenArray = {RandomUtil.randBool(), RandomUtil.randBool(), RandomUtil.randBool()};
        @BundleState
        byte[] mBytes = {RandomUtil.randInt8()};
        @BundleState
        short[] mShorts = {RandomUtil.randInt8(), RandomUtil.randInt8(), RandomUtil.randInt8()};
        @BundleState
        int[] mInts = {RandomUtil.randInt8(), RandomUtil.randInt8(), RandomUtil.randInt8()};
        @BundleState
        long[] mLongs = {RandomUtil.randInt8(), RandomUtil.randInt8(), RandomUtil.randInt8()};
        @BundleState
        float[] mFloats = {RandomUtil.randInt8(), RandomUtil.randInt8(), RandomUtil.randInt8()};
        @BundleState
        double[] mDoubles = {RandomUtil.randInt8(), RandomUtil.randInt8(), RandomUtil.randInt8()};
        @BundleState
        String[] mStrings = {RandomUtil.randShortString(), RandomUtil.randShortString(), RandomUtil.randShortString()};

        @BundleState
        ArrayList<String> mStringArrayList = new ArrayList<>(Arrays.asList(RandomUtil.randShortString(), RandomUtil.randShortString(), RandomUtil.randShortString()));
        @BundleState
        ArrayList<Integer> mIntegerArrayList = new ArrayList<>(Arrays.asList(RandomUtil.randInt32(), RandomUtil.randInt32(), RandomUtil.randInt32(), RandomUtil.randInt32()));

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SampleObject that = (SampleObject) o;

            if (mBoolean != that.mBoolean) return false;
            if (mByte != that.mByte) return false;
            if (mShort != that.mShort) return false;
            if (mInt != that.mInt) return false;
            if (mLong != that.mLong) return false;
            if (Float.compare(that.mFloat, mFloat) != 0) return false;
            if (Double.compare(that.mDouble, mDouble) != 0) return false;
            if (mString != null ? !mString.equals(that.mString) : that.mString != null)
                return false;
            if (!Arrays.equals(mBooleenArray, that.mBooleenArray)) return false;
            if (!Arrays.equals(mBytes, that.mBytes)) return false;
            if (!Arrays.equals(mShorts, that.mShorts)) return false;
            if (!Arrays.equals(mInts, that.mInts)) return false;
            if (!Arrays.equals(mLongs, that.mLongs)) return false;
            if (!Arrays.equals(mFloats, that.mFloats)) return false;
            if (!Arrays.equals(mDoubles, that.mDoubles)) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(mStrings, that.mStrings)) return false;
            if (mStringArrayList != null ? !mStringArrayList.equals(that.mStringArrayList) : that.mStringArrayList != null)
                return false;
            return mIntegerArrayList != null ? mIntegerArrayList.equals(that.mIntegerArrayList) : that.mIntegerArrayList == null;

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = (mBoolean ? 1 : 0);
            result = 31 * result + (int) mByte;
            result = 31 * result + (int) mShort;
            result = 31 * result + mInt;
            result = 31 * result + (int) (mLong ^ (mLong >>> 32));
            result = 31 * result + (mFloat != +0.0f ? Float.floatToIntBits(mFloat) : 0);
            temp = Double.doubleToLongBits(mDouble);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + (mString != null ? mString.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(mBooleenArray);
            result = 31 * result + Arrays.hashCode(mBytes);
            result = 31 * result + Arrays.hashCode(mShorts);
            result = 31 * result + Arrays.hashCode(mInts);
            result = 31 * result + Arrays.hashCode(mLongs);
            result = 31 * result + Arrays.hashCode(mFloats);
            result = 31 * result + Arrays.hashCode(mDoubles);
            result = 31 * result + Arrays.hashCode(mStrings);
            result = 31 * result + (mStringArrayList != null ? mStringArrayList.hashCode() : 0);
            result = 31 * result + (mIntegerArrayList != null ? mIntegerArrayList.hashCode() : 0);
            return result;
        }
    }
}
