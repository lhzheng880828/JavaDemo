package com.calvin.android.sample.msghandler;

import android.content.Context;
import android.os.Message;
import android.os.Parcel;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-8-8
 */
public class MsgRequest {

        static final String LOG_TAG = "RilRequest";

        //***** Class Variables
        static Random sRandom = new Random();
        static AtomicInteger sNextSerial = new AtomicInteger(0);
        private static Object sPoolSync = new Object();
        private static MsgRequest sPool = null;
        private static int sPoolSize = 0;
        private static final int MAX_POOL_SIZE = 4;
        private Context mContext;

        //***** Instance Variables
        int mSerial;
        int mRequest;
        Message mResult;
        Parcel mParcel;
        MsgRequest mNext;

        /**
         * Retrieves a new RILRequest instance from the pool.
         *
         * @param request RIL_REQUEST_*
         * @param result sent when operation completes
         * @return a RILRequest instance from the pool.
         */
        static MsgRequest obtain(int request, Message result) {
            MsgRequest rr = null;

            synchronized(sPoolSync) {
                if (sPool != null) {
                    rr = sPool;
                    sPool = rr.mNext;
                    rr.mNext = null;
                    sPoolSize--;
                }
            }

            if (rr == null) {
                rr = new MsgRequest();
            }

            rr.mSerial = sNextSerial.getAndIncrement();

            rr.mRequest = request;
            rr.mResult = result;
            rr.mParcel = Parcel.obtain();

            if (result != null && result.getTarget() == null) {
                throw new NullPointerException("Message target must not be null");
            }

            // first elements in any RIL Parcel
            rr.mParcel.writeInt(request);
            rr.mParcel.writeInt(rr.mSerial);

            return rr;
        }

        /**
         * Returns a RILRequest instance to the pool.
         *
         * Note: This should only be called once per use.
         */
        void release() {
            synchronized (sPoolSync) {
                if (sPoolSize < MAX_POOL_SIZE) {
                    mNext = sPool;
                    sPool = this;
                    sPoolSize++;
                    mResult = null;
                }
            }
        }

        private MsgRequest() {
        }

        static void
        resetSerial() {
            // use a random so that on recovery we probably don't mix old requests
            // with new.
            sNextSerial.set(sRandom.nextInt());
        }

        String
        serialString() {
            //Cheesy way to do %04d
            StringBuilder sb = new StringBuilder(8);
            String sn;

            long adjustedSerial = (((long)mSerial) - Integer.MIN_VALUE)%10000;

            sn = Long.toString(adjustedSerial);

            //sb.append("J[");
            sb.append('[');
            for (int i = 0, s = sn.length() ; i < 4 - s; i++) {
                sb.append('0');
            }

            sb.append(sn);
            sb.append(']');
            return sb.toString();
        }

        void
        onError(int error, Object ret) {
            CommandException ex;

            ex = CommandException.fromRilErrno(error);

            /*if (RIL.RILJ_LOGD) Rlog.d(LOG_TAG, serialString() + "< "
                    + RIL.requestToString(mRequest)
                    + " error: " + ex + " ret=" + RIL.retToString(mRequest, ret));*/

            if (mResult != null) {
                AsyncResult.forMessage(mResult, ret, ex);
                mResult.sendToTarget();
            }

            if (mParcel != null) {
                mParcel.recycle();
                mParcel = null;
            }
        }
}
