package com.example.vegitrp.fragments;

import com.example.vegitrp.models.Order;

public class AllRidesFragment extends BaseOrderListFragment {

    @Override
    protected boolean shouldIncludeOrder(Order order) {
        if (order == null || order.getStatus() == null) return false;
        
        // Drivers see all orders that are open or don't have a driver yet
        if ("Driver".equalsIgnoreCase(currentUserRole)) {
            return order.getAssignedDriverId() == null || currentUserId.equals(order.getAssignedDriverId());
        }

        if ("Buyer".equalsIgnoreCase(currentUserRole)) {
            return currentUserId.equals(order.getBuyerId());
        } else if ("Farmer".equalsIgnoreCase(currentUserRole)) {
            return currentUserId.equals(order.getAssignedFarmerId());
        }
        return false;
    }
}
