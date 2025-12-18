package com.tradeengine.engine.core.engine;

import com.tradeengine.engine.core.model.OrderSide;
import com.tradeengine.engine.core.model.OrderStatus;
import com.tradeengine.engine.persistence.entity.Order;
import jakarta.annotation.PostConstruct;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class Engine {

    private final TreeMap<BigDecimal, List<Order>> buyOrders = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<BigDecimal, List<Order>> sellOrders = new TreeMap<>();

    public void processOrder(Order newOrder) {
        System.out.println("📥 Ordin nou primit: " + newOrder.getSide() + " | Preț: " + newOrder.getPrice());

        if (newOrder.getSide() == OrderSide.BUY) {
            match(newOrder, sellOrders);
        } else {
            match(newOrder, buyOrders);
        }


        if (newOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            addOrderToBook(newOrder);
            System.out.println("📌 Ordinul a fost adăugat în Book. Rămas: " + newOrder.getRemainingQuantity());
        } else {
            System.out.println("✅ Ordin complet executat!");
        }

        printTrees();

    }

    private void match(Order newOrder, TreeMap<BigDecimal, List<Order>> oppositeBook) {        // Dacă e BUY, căutăm în SELL-uri (cel mai mic preț)

        while (newOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0 && !oppositeBook.isEmpty()) {
            BigDecimal bestOppositePrice = oppositeBook.firstKey();

            // Verificăm dacă prețurile se întâlnesc
            boolean canMatch = (newOrder.getSide() == OrderSide.BUY)
                    ? newOrder.getPrice().compareTo(bestOppositePrice) >= 0
                    : newOrder.getPrice().compareTo(bestOppositePrice) <= 0;

            if (!canMatch) break; // Prețurile nu se ating, ne oprim

            // Luăm lista de ordine la acel preț
            List<Order> ordersAtPrice = oppositeBook.get(bestOppositePrice);
            Iterator<Order> iterator = ordersAtPrice.iterator();

            while (iterator.hasNext() && newOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
                Order matchingOrder = iterator.next();

                // Calculăm cât putem tranzacționa (minimul dintre cele două)
                BigDecimal tradeQty = newOrder.getRemainingQuantity().min(matchingOrder.getRemainingQuantity());

                // Executăm tranzacția (scădem din ambele)
                newOrder.setRemainingQuantity(newOrder.getRemainingQuantity().subtract(tradeQty));
                matchingOrder.setRemainingQuantity(matchingOrder.getRemainingQuantity().subtract(tradeQty));

                // Aici vom genera un Trade Event mai târziu!
                System.out.println("Match găsit! Cantitate: " + tradeQty + " la prețul: " + bestOppositePrice);

                if (matchingOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
                    matchingOrder.setStatus(OrderStatus.FILLED);
                    iterator.remove(); // Ordinul vechi e gata, îl scoatem
                }
            }

            if (ordersAtPrice.isEmpty()) {
                oppositeBook.remove(bestOppositePrice);
            }
        }
    }

    private void addOrderToBook(Order order) {
        var book = (order.getSide() == OrderSide.BUY) ? buyOrders : sellOrders;
        book.computeIfAbsent(order.getPrice(), k -> new ArrayList<>()).add(order);

    }

    private void printTrees() {
        System.out.println("\n================ ORDER BOOK ================");

        // 1. Afișăm SELL ORDERS (ASK) - Cele mai mici prețuri jos, lângă mijloc
        System.out.println("--- SELL SIDE (ASKS) ---");
        if (sellOrders.isEmpty()) {
            System.out.println("  [ EMPTY ]");
        } else {
            // Folosim descendingKeySet ca să vedem prețurile mari sus și cele mici jos (spre mijloc)
            for (BigDecimal price : sellOrders.descendingKeySet()) {
                List<Order> orders = sellOrders.get(price);
                BigDecimal totalQty = orders.stream()
                        .map(Order::getRemainingQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                System.out.printf("  Price: %.2f | Qty: %.4f (%d orders)\n", price, totalQty, orders.size());
            }
        }

        System.out.println("--------------------------------------------");
        System.out.println("  ▲ SPREAD ▲  ");
        System.out.println("--------------------------------------------");

        // 2. Afișăm BUY ORDERS (BIDS) - Cele mai mari prețuri sus, lângă mijloc
        System.out.println("--- BUY SIDE (BIDS) ---");
        if (buyOrders.isEmpty()) {
            System.out.println("  [ EMPTY ]");
        } else {
            for (BigDecimal price : buyOrders.keySet()) {
                List<Order> orders = buyOrders.get(price);
                BigDecimal totalQty = orders.stream()
                        .map(Order::getRemainingQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                System.out.printf("  Price: %.2f | Qty: %.4f (%d orders)\n", price, totalQty, orders.size());
            }
        }
        System.out.println("============================================\n");
    }

    @PostConstruct
    public void init(){
        System.out.println("Trade engine started with success!");
    }
}
