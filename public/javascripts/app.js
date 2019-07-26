$(function () {

    // Comment author tooltips
    $('body').on('click', 'div.comment a[data-toggle="tooltip"]', function (event) {
        event.preventDefault();
        $(this).tooltip('hide');

        // Fold comments
        $('i', event.target).toggleClass('folded');
        $(event.target).parent('p').next('div.comment').toggleClass('d-none')

    })
    .tooltip({
        selector: '[data-toggle="tooltip"]'
    });

    // Comment folding
    $('div.comment', 'div#comments').prev('p').find('a > i').show();

    // Login form validation
    if (document.getElementById('form-login')) {

        window.addEventListener('load', function () {
            var form = document.getElementById('form-login');
            form.addEventListener('submit', function (e) {

                // Invalid submission
                if (form.checkValidity() === false) {
                    e.preventDefault();
                    e.stopPropagation();
                }

                // Valid submission
                else {
                    $('button', form).prop('disabled', true);
                }

                form.classList.add('was-validated');

            }, false);
        }, false);
    }

    // Toggle reply box
    $('span.reply', 'div.comment').click(function (e) {
        e.preventDefault();

        var $parent = $(this).parents('p'),
            $textareaInput = '' +
                '<div class="input-group">' +
                    '<textarea class="form-control rounded-0 reply-to" rows="2" aria-label="Reply" autofocus></textarea>' +
                    '<div class="input-group-append">' +
                        '<button class="btn btn-outline-primary" type="button">Reply</button>' +
                    '</div>' +
                '</div>';

        if ($parent.find('div.input-group').length) {
            $parent.find('div.input-group').remove(); // Toggle display
        }
        else {

            // Only show one reply box at one time
            $('span + div.input-group', 'div.comment').remove();

            // Display new reply box
            $parent.append($textareaInput);

            // If autofocus decides not to work
            $('textarea:visible').focus();
        }
    });

    // Reply box response
    $('div.comment').on('click', 'button', function (e) {
        e.stopPropagation();

        var $parent = $(this).parents('div.input-group'),
            replyData = $parent.prev('span.reply').data(),
            postid = replyData.postid,
            commentid = replyData.commentid,
            comment = $parent.find('textarea.reply-to').val(),
            csrfToken = $(':hidden[name=csrfToken]', 'section.article-body article').val();

        if (comment.trim().length) {

            var route = jsRoutes.controllers.BlogPostController.addComment(postid, commentid, comment);

            $.ajax({
                method: 'post',
                url: route.url,
                contentType : 'application/json',
                beforeSend: function(xhr) {

                    // Show loading animation
                    var loadingIcon = '' +
                        '<div class="loading-icon d-flex waiting-indicator">' +
                        '   <div class="spinner-grow text-danger justify-content-center align-self-center" role="status" aria-hidden="true"></div>' +
                        '</div>';

                    $('textarea', 'div.comment div.input-group:visible').prop('readonly', true)
                    $('button', 'div.comment div.input-group:visible').prop('disabled', true).html(loadingIcon);

                    xhr.setRequestHeader('Csrf-Token', csrfToken);
                },
                error: function(xhr, status, error) {
                    // @TODO Implement error callback (session expired, etc.)
                },
                success: function(data, status, xhr) {

                    // Send the response data over the WebSocket connection
                    connection.send(JSON.stringify(data));

                    // Supplement reply box with new comment
                    $('span + div.input-group:visible', 'div.comment').slideUp(function() {

                        // Remove reply box
                        $replyBox = $(this);
                        $replyLink = $replyBox.prev('span.reply');
                        $commentingOn = $replyLink.parent('p');
                        $replyBox.remove();

                        // Copy-pasta from displayComments reusable block in the post template
                        var $newComment = $('' +
                            '<p id="comment-anchor-' + data.commentid + '">' +
                                '<a href="#" data-placement="top" data-toggle="tooltip" title="' + data.name + '">' +
                                    '<i></i>' +
                                    data.handle +
                                '</a>' +
                                '<span>' +
                                    '<span>·</span>' +
                                    'Just now' +
                                    '<span>·</span>' +
                                '</span>' +
                                data.comment +
                            '</p>');

                        // Display new comments
                        // Note: Subsequent refreshes will display at the bottom of parent thread bottom (as its older).

                        // 1.) Replied directly to post- inject new comment at the top of the comments section.
                        if (parseInt(($replyLink).data('commentid')) === 0) {
                            $newComment.wrap('<div class="comment"></div>').parent().insertAfter($('div#comments > hr'));
                        }

                        // 2.) Replied to a comment that already has sibling comments
                        else if ($commentingOn.next().is('div.comment')) {
                            $commentingOn.next('div.comment').prepend($newComment);
                        }

                        // 3.) First nested comment (Needs to be wrapped in a <div class="comment">)
                        else {
                            $newComment.wrap('<div class="comment"></div>').parent().insertAfter($commentingOn);

                            // Give the parent the toggle symbol
                            $newComment.parent('div.comment').prev('p').children('a').children('i').css('display', 'inline');
                        }

                        animateComment($newComment);
                    });
                }
            });
        }
    });

    // Animate page anchor comments
    if (window.location.hash) {
        if ($(window.location.hash).is('p')) {
            animateComment($(window.location.hash));
        }
    }

    // Chat/WebSocket configuration
    if ('WebSocket' in window && typeof window.webSocketUrl !== 'undefined') {

        var connection = new WebSocket(webSocketUrl);

        connection.onmessage = function(event) {

            // For same-page links- we want to refresh the page
            var urlParams = new URLSearchParams(window.location.search),
                refreshParam = (parseInt(urlParams.get('refresh')) || 0) + 1;

            // Massage data passed from Ajax request
            var data = JSON.parse(event.data),
                anchor = 'comment-anchor-' + data.commentid,
                handle = data.handle,
                title = data.title,
                link = window.location.origin + '/blog/' + data.slug + '?refresh=' + refreshParam + '#comment-anchor-' + data.commentid;

            // If it's a keep-alive- don't do anything
            if (data.keepAlive) return;

            // Create notification
            var toast = '' +
                '<div id="' + anchor + '" class="toast" data-autohide="true" data-delay="10000" role="alert" aria-live="assertive" aria-atomic="true">' +
                    '<div class="toast-header">' +
                        '<span class="mr-auto">New Comment</span>' +
                        '<button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">' +
                            '<span aria-hidden="true">&times;</span>' +
                        '</button>' +
                    '</div>' +
                    '<div class="toast-body">' +
                        handle + ' just commented on:<br />"' + title + '".<br /><br />' +
                        '<a href="' + link + '">Click here to see it.</a>' +
                    '</div>' +
                '</div>';

            // Stack notification and display
            $('div#notification-area').prepend(toast);
            $('#' + anchor).toast('show');
        };

        connection.onclose = function() {
            // @TODO Implement onclose WebSocket event handler (WebSocket is already in CLOSING or CLOSED state.)
        };

        connection.onopen = function() {
            // @TODO Implement onopen WebSocket event handler (socket open indicator, etc.)
        };

        connection.onerror = function(error) {
            // @TODO Implement onerror WebSocket event handler
        };

        // Send WebSocket keep-alive every 30s
        setInterval(function() {
            connection.send(JSON.stringify({ 'keepAlive': true }));
            console.log('Keep-alive sent.');
        }, 30000);
    }

    // Helper function to animate comments
    function animateComment($newComment) {
        $newComment.addClass('active-page-anchor');

        setTimeout(function() {
            $newComment.removeClass('active-page-anchor').addClass('inactive-page-anchor');
        }, 1000);
    }
});
