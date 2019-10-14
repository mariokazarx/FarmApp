package com.example.farmapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.farmapp.BaseViewHolder;
import com.example.farmapp.InfoActivity;
import com.example.farmapp.R;
import com.example.farmapp.vo.Device;


import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class DeviceAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static final String TAG = "DeviceAdapter";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final int VIEW_TYPE_EMPTY = 0;
    public static final int VIEW_TYPE_NORMAL = 1;

    private Callback mCallback;
    private List<Device> mDevicetList;

    public DeviceAdapter(List<Device> deviceList) {
        mDevicetList = deviceList;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.device_card, parent, false));
            case VIEW_TYPE_EMPTY:
            default:
                return new EmptyViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty_view, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDevicetList != null && mDevicetList.size() > 0) {
            return VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_EMPTY;
        }
    }

    @Override
    public int getItemCount() {
        if (mDevicetList != null && mDevicetList.size() > 0) {
            return mDevicetList.size();
        } else {
            return 1;
        }
    }

    public void addItems(List<Device> deviceList) {
        mDevicetList.addAll(deviceList);
        notifyDataSetChanged();
    }

    public interface Callback {
        void onEmptyViewRetryClick();
    }

    public class ViewHolder extends BaseViewHolder {

        TextView nameTextView;

        TextView addressTextView;


        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.deviceName);
            addressTextView = itemView.findViewById(R.id.deviceAddress);
        }

        protected void clear() {
            nameTextView.setText("");
            addressTextView.setText("");
        }

        public void onBind(int position) {
            super.onBind(position);

            final Device mDevice = mDevicetList.get(position);

            if (mDevice.getName() != null) {
                nameTextView.setText(mDevice.getName());
            }

            if (mDevice.getAddress() != null) {
                addressTextView.setText(mDevice.getAddress());
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent i = new Intent(view.getContext(), InfoActivity.class);
                    i.putExtra(EXTRA_DEVICE_ADDRESS, mDevice.getAddress());
                    view.getContext().startActivity(i);
                }
            });
        }
    }

    public class EmptyViewHolder extends BaseViewHolder {

        TextView messageTextView;
        Button buttonRetry;

        EmptyViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_message);
            buttonRetry = itemView.findViewById(R.id.buttonRetry);
            buttonRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    mCallback.onEmptyViewRetryClick();
                }
            });
        }

        @Override
        protected void clear() {

        }

    }
}
