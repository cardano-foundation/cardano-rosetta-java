class RosettaClientError(Exception):
    """Base exception for Rosetta client errors"""
    pass

class NetworkError(RosettaClientError):
    """Raised when network communication fails"""
    pass

class ValidationError(RosettaClientError):
    """Raised when transaction validation fails"""
    pass 