class Ajax {

    static post(url, data) {
        return fetch(url, {
            body: JSON.stringify(data),
            headers: {
                'content-type': 'application/json'
            },
            method: 'POST', // *GET, POST, PUT, DELETE, etc.
            mode: 'cors', // no-cors, cors, *same-origin
            redirect: 'follow', // manual, *follow, error
            referrer: 'no-referrer', // *client, no-referrer
        }).then(response => {
            if (response.ok) {
                return response.json()
            } else {
                throw new Error('Network response was not ok.');
            }
        })
    }

    static get(url, data) {

    }
}

module.exports = Ajax;