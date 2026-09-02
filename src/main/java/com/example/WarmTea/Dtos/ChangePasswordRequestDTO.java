package com.example.WarmTea.Dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChangePasswordRequestDTO {
    private String oldPassword;
    private String newPassword;

    public ChangePasswordRequestDTO() {}

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    @Override
    public String toString() {
        return "ChangePasswordRequestDTO{oldPassword='***', newPassword='***'}";
    }
}