class RosettaClientError(Exception):
    """Base exception for Rosetta client errors"""
    pass

class NetworkError(RosettaClientError):
    """Raised when network communication fails"""
    pass

class ValidationError(RosettaClientError):
    """Raised when transaction validation fails"""
    pass

class BalanceError(RosettaClientError):
    """Raised when balance validation fails"""
    pass

class TransactionError(RosettaClientError):
    """Raised when transaction processing fails"""
    pass

class TimeoutError(RosettaClientError):
    """Raised when an operation times out"""
    pass 