package actors;

public interface ActorMessage<RequestDataType, ResponseDataType> {
    Class<ResponseDataType> getResponseDataType();

    Class<RequestDataType> getRequestDataType();
}
