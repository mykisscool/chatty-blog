$(function () {

    // Comment author tooltips
    var $tooltips = $('a[data-toggle="tooltip"]', 'div.comment');
    $tooltips
        .click(function (event) {
            event.preventDefault();
            $tooltips.tooltip('hide');

            // Fold comments
            $('i', event.target).toggleClass('folded');
            $(event.target).parent('p').next('div.comment').toggleClass('d-none')

        })
        .tooltip();

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
                    '<textarea class="form-control rounded-0 reply-to" rows="2" aria-label="Reply"></textarea>' +
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

            // @TODO Implement error callback (session expired, etc.)
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
                success: function(data, status, xhr) {

                    // Remove reply box
                    $('span + div.input-group:visible', 'div.comment').slideUp(function() {
                        $(this).remove();

                        // @TODO Show new comment
                    });
                }
            });
        }
    });
});
