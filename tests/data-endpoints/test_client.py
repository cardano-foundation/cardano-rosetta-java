import httpx

from client import RosettaClient


def _json_response(status_code, payload):
    return httpx.Response(status_code, json=payload, request=httpx.Request("POST", "http://test"))


def test_post_retries_retriable_indexer_not_ready(monkeypatch):
    monkeypatch.setenv("ROSETTA_INDEXER_NOT_READY_RETRY_ATTEMPTS", "2")
    monkeypatch.setenv("ROSETTA_INDEXER_NOT_READY_RETRY_DELAY_SECONDS", "0")

    responses = [
        _json_response(
            503,
            {
                "code": 5056,
                "message": "This endpoint is unavailable until the indexer data is ready.",
                "retriable": True,
            },
        ),
        _json_response(200, {"transactions": [], "total_count": 0}),
    ]

    client = RosettaClient(default_network="preprod", validate_schemas=False)
    monkeypatch.setattr(client.client, "post", lambda *args, **kwargs: responses.pop(0))

    response = client._post("/search/transactions", {})

    assert response.status_code == 200
    assert len(responses) == 0


def test_post_does_not_retry_non_retriable_indexer_not_ready(monkeypatch):
    monkeypatch.setenv("ROSETTA_INDEXER_NOT_READY_RETRY_ATTEMPTS", "2")
    monkeypatch.setenv("ROSETTA_INDEXER_NOT_READY_RETRY_DELAY_SECONDS", "0")

    responses = [
        _json_response(
            503,
            {
                "code": 5056,
                "message": "This endpoint is unavailable until the indexer data is ready.",
                "retriable": False,
            },
        ),
        _json_response(200, {"transactions": [], "total_count": 0}),
    ]

    client = RosettaClient(default_network="preprod", validate_schemas=False)
    monkeypatch.setattr(client.client, "post", lambda *args, **kwargs: responses.pop(0))

    response = client._post("/search/transactions", {})

    assert response.status_code == 503
    assert len(responses) == 1
