package nl.bvsit.coworker.service;

import nl.bvsit.coworker.domain.CpMenuItem;
import nl.bvsit.coworker.domain.OrderItem;
import nl.bvsit.coworker.payload.OrderItemDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CpMenuItemUtil {
    public static List<Long> getIds(Collection<CpMenuItem> cpMenuItems){
        return cpMenuItems.stream()
                .map(CpMenuItem::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<Long> getOrderItemDtoIds(Collection<OrderItemDTO> orderItems){
        return orderItems.stream()
                .map(OrderItemDTO::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<CpMenuItem> getMenuItems(Collection<OrderItem> orderItems){
        List<CpMenuItem> menuItems=new ArrayList<>();
        for(OrderItem orderItem: orderItems){
            menuItems.add(orderItem.getId().getItem());
        }
        return menuItems;
    }

    public static List<Long> getMenuItemIds(Collection<OrderItem> orderItems){
        return getIds(getMenuItems(orderItems));
    }
}
