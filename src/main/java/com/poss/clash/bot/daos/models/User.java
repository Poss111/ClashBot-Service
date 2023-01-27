package com.poss.clash.bot.daos.models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.poss.clash.bot.converter.UserSubscriptionTypeConverter;
import com.poss.clash.bot.openapi.model.Role;
import com.poss.clash.bot.openapi.model.Subscription;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@DynamoDBTable(tableName = "User")
public class User {

    private String id;
    private String name;
    private Role role;
    private ArrayList<String> champions = new ArrayList<>();
    private HashMap<String, Boolean> subscriptions = new HashMap<>();
    private String serverName;

    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    @DynamoDBAttribute(attributeName = "role")
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @DynamoDBAttribute(attributeName = "champions")
    public ArrayList<String> getChampions() {
        return champions;
    }

    public void setChampions(ArrayList<String> champions) {
        this.champions = champions;
    }

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
    @DynamoDBAttribute(attributeName = "subscription")
    public HashMap<String, Boolean> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(HashMap<String, Boolean> subscriptions) {
        this.subscriptions = subscriptions;
    }

    @DynamoDBAttribute(attributeName = "serverName")
    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
