package com.example.clientandroidaudiobookapplication.models;

public class ActorVoicesResponse {
    private int id;
    private String nameActor;

    public ActorVoicesResponse(int id, String nameActor) {
        this.id = id;
        this.nameActor = nameActor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameActor() {
        return nameActor;
    }

    public void setNameActor (String nameActor) {
        this.nameActor = nameActor;
    }
}
