package com.example.vegitrp.fragments;

import com.example.vegitrp.models.Order;

public class CompletedFragment extends BaseOrderListFragment {

    @Override
    protected boolean shouldIncludeOrder(Order order) {
        if (order == null || order.getStatus() == null) return false;
        
        boolean isCompleted = "COMPLETED".equalsIgnoreCase(order.getStatus()) || "DELIVERED".equalsIgnoreCase(order.getStatus());
        
        if ("Buyer".equalsIgnoreCase(currentUserRole)) {
            return isCompleted && currentUserId.equals(order.getBuyerId());
        } else if ("Farmer".equalsIgnoreCase(currentUserRole)) {
            return isCompleted && currentUserId.equals(order.getAssignedFarmerId());
        } else if ("Driver".equalsIgnoreCase(currentUserRole)) {
            return isCompleted && currentUserId.equals(order.getAssignedDriverId());
        }
        return false;
    }
}
