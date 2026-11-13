package com.example.dbms.model;

/**
 * Модель героя Dota 2 (таблица {@code heroes}).
 */
public class Hero {

    private int id;
    private String name;
    private boolean side;
    private int hp;
    private String attribute;
    private boolean isAlive;

    /**
     * Создаёт героя с неинициализированными полями.
     */
    public Hero() {
    }

    /**
     * Создаёт героя со всеми полями.
     *
     * @param id        первичный ключ
     * @param name      имя героя
     * @param side      {@code true} — Radiant, {@code false} — Dire
     * @param hp        количество здоровья
     * @param attribute основной атрибут (например, Strength, Agility, Intelligence)
     * @param isAlive   жив ли герой
     */
    public Hero(int id, String name, boolean side, int hp, String attribute, boolean isAlive) {
        this.id = id;
        this.name = name;
        this.side = side;
        this.hp = hp;
        this.attribute = attribute;
        this.isAlive = isAlive;
    }

    /**
     * Возвращает идентификатор героя.
     *
     * @return первичный ключ
     */
    public int getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор героя.
     *
     * @param id первичный ключ
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Возвращает имя героя.
     *
     * @return имя
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает имя героя.
     *
     * @param name имя
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает сторону героя.
     *
     * @return {@code true}, если Radiant; {@code false}, если Dire
     */
    public boolean isSide() {
        return side;
    }

    /**
     * Устанавливает сторону героя.
     *
     * @param side {@code true} — Radiant, {@code false} — Dire
     */
    public void setSide(boolean side) {
        this.side = side;
    }

    /**
     * Возвращает здоровье героя.
     *
     * @return HP
     */
    public int getHp() {
        return hp;
    }

    /**
     * Устанавливает здоровье героя.
     *
     * @param hp HP
     */
    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * Возвращает основной атрибут героя.
     *
     * @return атрибут
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * Устанавливает основной атрибут героя.
     *
     * @param attribute атрибут
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    /**
     * Возвращает, жив ли герой.
     *
     * @return {@code true}, если герой жив
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Устанавливает, жив ли герой.
     *
     * @param alive {@code true}, если герой жив
     */
    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    /**
     * Возвращает строковое представление героя.
     *
     * @return описание всех полей
     */
    @Override
    public String toString() {
        return "Hero{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", side=" + side
                + ", hp=" + hp
                + ", attribute='" + attribute + '\''
                + ", isAlive=" + isAlive
                + '}';
    }
}
