package com.app.entity;


public enum  FlyStatus {
    SENT("Отправлен"),DELAYED("Задержаный"),REGISTRATION("Регистрация"),
    SCHEDULED("По расписанию"),CANCELED("Отменен"),
    ARRIVED("Прибыл"),EXPECTED("Ожидается");
    private String name;

    FlyStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public static FlyStatus valueOfName(String name){
        for(FlyStatus status:FlyStatus.values()){
            if (status.getName().equals(name)) return status;
        }
        return null;
    }
}
