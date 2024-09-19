package com.batch.settle.domain;

public enum ApiProduct {
    A(1L, "api/test/A", 100),
    B(2L, "api/test/B", 200),
    C(3L, "api/test/C", 300),
    D(4L, "api/test/D", 400),
    E(5L, "api/test/E", 500),
    F(6L, "api/test/F", 600),
    G(7L, "api/test/G", 700),
    H(8L, "api/test/H", 800),
    I(9L, "api/test/I", 900),
    J(10L, "api/test/J", 1000),
    K(11L, "api/test/K", 1100),
    L(12L, "api/test/L", 1200),
    M(13L, "api/test/M", 1300),
    N(14L, "api/test/N", 1400),
    O(15L, "api/test/O", 1500),
    P(16L, "api/test/P", 1600),
    Q(17L, "api/test/Q", 1700),
    R(18L, "api/test/R", 1800),
    S(19L, "api/test/S", 1900),
    T(20L, "api/test/T", 2000),
    U(21L, "api/test/U", 2100),
    V(22L, "api/test/V", 2200),
    W(23L, "api/test/W", 2300),
    X(24L, "api/test/X", 2400),
    Y(25L, "api/test/Y", 2500),
    Z(26L, "api/test/Z", 2600);
    private final Long id;
    private final String url;
    private final Integer fee;

    ApiProduct(Long id, String url, Integer fee) {
        this.id = id;
        this.url = url;
        this.fee = fee;
    }

    public Long id() {
        return id;
    }

    public String url() {
        return url;
    }

    public Integer fee() {
        return fee;
    }
}
