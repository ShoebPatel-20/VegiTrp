package com.example.vegitrp.fragments;

import com.example.vegitrp.models.Order;

public class ProcessFragment extends BaseOrderListFragment {

    @Override
    protected boolean shouldIncludeOrder(Order order) {
        if (order == null || order.getStatus() == null) return false;
        
        boolean isAccepted = "ACCEPTED".equalsIgnoreCase(order.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(order.getStatus());
                             
        if ("Buyer".equalsIgnoreCase(currentUserRole)) {
            return isAccepted && currentUserId.equals(order.getBuyerId());
        } else if ("Farmer".equalsIgnoreCase(currentUserRole)) {
            return isAccepted && currentUserId.equals(order.getAssignedFarmerId());
        } else if ("Driver".equalsIgnoreCase(currentUserRole)) {
            return isAccepted && currentUserId.equals(order.getAssignedDriverId());
        }
        return false;
    }
}
