# ADR-002: License the project under AGPLv3

## Status

Accepted, 2026-10-21

## Context

This app is an educational project with no commercial plans. Milestone 7 plans to integrate libsignal into the Android app.  
libsignal is licensed under AGPLv3 (https://github.com/signalapp/libsignal/blob/main/README.md), so I  need clear licensing terms for my  
own code before that integration.

## Decision

I will license the original backend code that I write under the GNU Affero General Public License version 3 (https://www.gnu.org/licenses/agpl-3.0.html),  
using the identifier AGPL-3.0-only. I will add the complete license test to README file of the project. Third-party components keep their own  
copyright notices and license terms.

## Consequences

- When I distribute an APK containing libsignal, recipients must be able to obtain its Corresponding Source and required license notices.
- People may use, study, modify and redistribute my code under AGPLv3. The license also permits commercial use.
- Modified AGPL-covered server programs must offer their source to users who interact with them over a network, as described in the license text (https://www.gnu.org/licenses/agpl-3.0.html).
- Signal does not support libsignal use outside its own apps (https://github.com/signalapp/libsignal/blob/main/README.md), so I must maintain and test my integration myself.
