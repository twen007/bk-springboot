/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author ynz25
 */
// WsCallFailedRecord is designed to support general web service calls
// Currently only IBBR is specified
public enum WsCategory {
    IBBR(1), UNSUPPROTED(-1);
    private final int value;

    private WsCategory(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private static final Map<Integer, WsCategory> map;

    static {
        map = Arrays.stream(values())
                .collect(Collectors.toMap(e -> e.value, e -> e));
    }

    public static WsCategory fromInt(int value) {
        return Optional.ofNullable(map.get(value)).orElse(UNSUPPROTED);
    }
}
