package zjc.starless.alipaydemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应类
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> GenericResponse<T> success(T data) {
        return new GenericResponse<>(200, "操作成功", data);
    }

    public static <T> GenericResponse<T> error(String message) {
        return new GenericResponse<>(500, message, null);
    }
}