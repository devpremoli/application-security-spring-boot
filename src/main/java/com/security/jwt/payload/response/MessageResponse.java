package com.security.jwt.payload.response;

/**
 * MESSAGE RESPONSE DTO
 *
 * A simple response object for sending messages back to the client
 * Used for success messages, error messages, or any simple text response
 *
 * Response JSON format:
 * {
 *   "message": "User registered successfully!"
 * }
 *
 * Common use cases:
 * 1. Signup success: {"message": "User registered successfully!"}
 * 2. Validation error: {"message": "Username is already taken!"}
 * 3. Generic error: {"message": "An error occurred. Please try again."}
 * 4. Success action: {"message": "Profile updated successfully!"}
 *
 * Why use a DTO for just a message?
 * 1. Consistency: All API responses have same structure
 * 2. Extensibility: Easy to add more fields later (e.g., code, timestamp)
 * 3. Type safety: Clear contract between client and server
 * 4. Documentation: Swagger/OpenAPI can document the structure
 */
public class MessageResponse {

    /*
     * MESSAGE - The text message to send to client
     */
    private String message;

    /*
     * CONSTRUCTOR
     *
     * Takes a message string and creates the response object
     */
    public MessageResponse(String message) {
        this.message = message;
    }

    /*
     * GETTERS AND SETTERS
     */

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
