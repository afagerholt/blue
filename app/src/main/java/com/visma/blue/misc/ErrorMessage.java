package com.visma.blue.misc;

import com.visma.blue.R;
import com.visma.blue.network.OnlineResponseCodes;

public class ErrorMessage {
    public static int getErrorMessage(int onlineResponseCode, boolean isUploadingPhoto) {
        int errorId = R.string.visma_blue_error_unknown_error;

        switch (onlineResponseCode) {
            case OnlineResponseCodes.ConnectionTimeOut:
                errorId = R.string.visma_blue_error_connection_time_out;
                break;
            case OnlineResponseCodes.UnknownError:
                if (isUploadingPhoto) {
                    errorId = R.string.visma_blue_error_unknown_error_when_sending;
                } else {
                    errorId = R.string.visma_blue_error_unknown_error;
                }
                break;
            case OnlineResponseCodes.InvalidUserOrPassword:
                errorId = R.string.visma_blue_error_invalid_user_or_password;
                break;
            case OnlineResponseCodes.NoAccessToApplication:
                if (isUploadingPhoto) {
                    errorId = R.string.visma_blue_error_no_access_to_application_when_sending;
                } else {
                    errorId = R.string.visma_blue_error_no_access_to_application;
                }
                break;
            case OnlineResponseCodes.InvalidToken:
                errorId = R.string.visma_blue_error_invalid_token;
                break;
            case OnlineResponseCodes.LicenceAgreementNotAccepted:
                errorId = R.string.visma_blue_error_license_agreement_not_accepted;
                break;
            // Not used
            case OnlineResponseCodes.InvalidClient:
                break;
            // Not used
            case OnlineResponseCodes.NewClientRequired:
                break;
            case OnlineResponseCodes.InvalidPhoto:
                errorId = R.string.visma_blue_error_invalid_photo;
                break;
            case OnlineResponseCodes.InvalidState:
                errorId = R.string.visma_blue_error_invalid_state;
                break;
            case OnlineResponseCodes.BlockedCompany:
                if (isUploadingPhoto) {
                    errorId = R.string.visma_blue_error_blocked_company_when_sending;
                } else {
                    errorId = R.string.visma_blue_error_blocked_company;
                }
                break;
            case OnlineResponseCodes.UserIsLocked:
                errorId = R.string.visma_blue_error_user_is_locked;
                break;
            case OnlineResponseCodes.NotSet:
            default:
                if (isUploadingPhoto) {
                    errorId = R.string.visma_blue_error_unknown_error_when_sending;
                } else {
                    errorId = R.string.visma_blue_error_unknown_error;
                }
                break;
        }

        return errorId;
    }
}
