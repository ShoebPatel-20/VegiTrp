package com.example.vegitrp.fragments;

import com.example.vegitrp.models.Order;

public class InterestFragment extends BaseOrderListFragment {

    @Override
    protected boolean shouldIncludeOrder(Order order) {
        if (order == null || order.getStatus() == null) return false;
        
        // Status might be "INTERESTED" or just pending acceptance by farmer/driver
        boolean isInterested = "INTERESTED".equalsIgnoreCase(order.getStatus()) || "PENDING".equalsIgnoreCase(order.getStatus());
        
        if ("Buyer".equalsIgnoreCase(currentUserRole)) {
            return isInterested && currentUserId.equals(order.getBuyerId());
        } else if ("Farmer".equalsIgnoreCase(currentUserRole)) {
            // Farmers see orders they have expressed interest in (if logic exists) or that are for them
            return isInterested && currentUserId.equals(order.getAssignedFarmerId());
        } else if ("Driver".equalsIgnoreCase(currentUserRole)) {
            // Drivers see orders where they are assigned or interested
            return isInterested && currentUserId.equals(order.getAssignedDriverId());
        }
        return false;
    }
}
