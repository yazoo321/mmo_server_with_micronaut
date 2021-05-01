package server.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {

    @Size(min = 3, max = 20)
    @NonNull
    String username;

    @Size(min = 3, max = 45)
    @NonNull
    String email;

    @Size(min = 3, max = 45)
    @NonNull
    String password;

}
