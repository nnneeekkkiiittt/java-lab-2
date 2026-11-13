package com.example.dbms.model;

import java.time.LocalDate;

/**
 * Модель предмета Dota 2 (таблица {@code items}), связанного с героем по {@code hero_id}.
 */
public class Item {

    private int id;
    private String name;
    private int heroId;
    private LocalDate purchaseDate;
    private double price;

    /**
     * Создаёт предмет с неинициализированными полями.
     */
    public Item() {
    }

    /**
     * Создаёт предмет со всеми полями.
     *
     * @param id            первичный ключ
     * @param name          название предмета
     * @param heroId        внешний ключ на {@link Hero#getId()}
     * @param purchaseDate  дата покупки
     * @param price         цена
     */
    public Item(int id, String name, int heroId, LocalDate purchaseDate, double price) {
        this.id = id;
        this.name = name;
        this.heroId = heroId;
        this.purchaseDate = purchaseDate;
        this.price = price;
    }

    /**
     * Возвращает идентификатор предмета.
     *
     * @return первичный ключ
     */
    public int getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор предмета.
     *
     * @param id первичный ключ
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Возвращает название предмета.
     *
     * @return название
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название предмета.
     *
     * @param name название
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает идентификатор героя-владельца.
     *
     * @return внешний ключ на героя
     */
    public int getHeroId() {
        return heroId;
    }

    /**
     * Устанавливает идентификатор героя-владельца.
     *
     * @param heroId внешний ключ на героя
     */
    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    /**
     * Возвращает дату покупки предмета.
     *
     * @return дата покупки
     */
    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    /**
     * Устанавливает дату покупки предмета.
     *
     * @param purchaseDate дата покупки
     */
    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    /**
     * Возвращает цену предмета.
     *
     * @return цена
     */
    public double getPrice() {
        return price;
    }

    /**
     * Устанавливает цену предмета.
     *
     * @param price цена
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Возвращает строковое представление предмета.
     *
     * @return описание всех полей
     */
    @Override
    public String toString() {
        return "Item{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", heroId=" + heroId
                + ", purchaseDate=" + purchaseDate
                + ", price=" + price
                + '}';
    }
}
